package io.github.wasabithumb.xpdy.endpoint;

import io.github.wasabithumb.xpdy.except.ContextMissException;
import io.github.wasabithumb.xpdy.except.handling.ExceptionHandler;
import io.github.wasabithumb.xpdy.logging.XpdyLogger;
import io.github.wasabithumb.xpdy.misc.HTTPVerb;
import io.github.wasabithumb.xpdy.misc.MimeType;
import io.github.wasabithumb.xpdy.misc.path.PathMap;
import io.github.wasabithumb.xpdy.payload.request.Request;
import io.github.wasabithumb.xpdy.payload.response.Response;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

@ApiStatus.Internal
public class EndpointRegistry {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final PathMap<Map<HTTPVerb, Registration>> map = new PathMap<>();
    private final XpdyLogger logger;
    private final String defaultIn;
    private final String defaultOut;
    private final ExceptionHandler exceptionHandler;

    public EndpointRegistry(
            @NotNull XpdyLogger logger,
            @NotNull @MimeType String defaultIn,
            @NotNull @MimeType String defaultOut,
            @NotNull ExceptionHandler exceptionHandler
    ) {
        this.logger = logger;
        this.defaultIn = defaultIn;
        this.defaultOut = defaultOut;
        this.exceptionHandler = exceptionHandler;
    }

    //

    public @NotNull Response invokeEndpoint(
            @NotNull String method,
            @NotNull String path,
            @NotNull Function<List<String>, Request> createRequest
    ) {
        return this.invokeEndpoint(new VerbLike.Arbitrary(method), path, createRequest);
    }

    public @NotNull Response invokeEndpoint(
            @NotNull HTTPVerb method,
            @NotNull String path,
            @NotNull Function<List<String>, Request> createRequest
    ) {
        return this.invokeEndpoint(new VerbLike.Literal(method), path, createRequest);
    }

    private @NotNull Response invokeEndpoint(
            @NotNull VerbLike method,
            @NotNull String path,
            @NotNull Function<List<String>, Request> createRequest
    ) {
        Registration r;
        List<String> params;
        HTTPVerb verb;

        this.lock.readLock().lock();
        try {
            PathMap.Resolution<Map<HTTPVerb, Registration>> resolution = this.map.resolve(path);
            if (resolution == null) {
                this.logger.log(404, method.toString(), path, null);
                return Response.error(404);
            }
            params = resolution.params();

            Map<HTTPVerb, Registration> sub = resolution.value();
            if (method.isMeta()) {
                this.logger.log(204, method.toString(), path, null);
                return this.serveMeta(sub, true);
            }

            verb = method.toVerb();
            if (verb == null || (r = sub.get(verb)) == null) {
                this.logger.log(405, method.toString(), path, null);
                return this.serveMeta(sub, false);
            }
        } finally {
            this.lock.readLock().unlock();
        }

        EndpointContext ctx = new EndpointContext(createRequest.apply(params), r.meta);
        Response response;
        Throwable error = null;
        try {
            r.registrar.beforeEach(ctx);
            response = this.serveInternal(r, ctx);
            ctx.setResponse(response);
            r.registrar.afterEach(ctx);
            response = ctx.getResponse();
        } catch (Throwable t) {
            error = t;
            response = Response.error(this.exceptionHandler.handle(t));
        }

        this.logger.log(response.code(), method.toString(), path, error);

        if (!verb.equals(HTTPVerb.GET) && !response.headers().containsKey("Accept")) {
            response.headers().set("Accept", r.meta.inType(this.defaultIn));
        }
        return response;
    }

    private @NotNull Response serveMeta(@NotNull Map<HTTPVerb, Registration> map, boolean ok) {
        StringBuilder allow = new StringBuilder("HEAD, OPTIONS");
        String in = this.defaultIn;
        for (Map.Entry<HTTPVerb, Registration> entry : map.entrySet()) {
            allow.append(", ").append(entry.getKey().name());
            if (entry.getKey().equals(HTTPVerb.GET)) continue;
            //noinspection PatternValidation
            in = entry.getValue().meta.inType(in);
        }

        Response.Builder builder = Response.builder()
                .code(ok ? 204 : 405)
                .setHeader("Accept", in)
                .setHeader("Allow", allow.toString());

        if (ok) {
            String out = this.defaultOut;
            Registration r = map.get(HTTPVerb.GET);
            if (r != null) {
                //noinspection PatternValidation
                out = r.meta.outType(out);
            }
            builder.setHeader("Content-Type", out);
        }

        return builder.build();
    }

    private @NotNull Response serveInternal(@NotNull Registration r, @NotNull EndpointContext ctx) throws Throwable {
        Method m = r.method;
        Parameter[] params = m.getParameters();
        int paramCount = params.length;
        Object[] args = new Object[paramCount];

        for (int i=0; i < paramCount; i++) {
            Parameter param = params[i];
            Object arg = ctx.get(param.getType());
            if (arg == null)
                throw new ContextMissException(m, param);
            args[i] = arg;
        }

        Object out;
        try {
            out = r.method.invoke(r.registrar, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause != null) throw cause;
            throw e;
        }

        if (r.meta.isVoid()) {
            return Response.builder()
                    .code(200)
                    .build();
        }

        return (Response) out;
    }

    //

    public void registerEndpoints(@NotNull Endpoints endpoints) {
        this.lock.writeLock().lock();
        try {
            Class<?> cls = endpoints.getClass();
            do {
                this.registerAt(cls, endpoints);
                cls = cls.getSuperclass();
            } while (cls != null && Endpoints.class.isAssignableFrom(cls));
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private void registerAt(@NotNull Class<?> cls, @NotNull Endpoints endpoints) {
        EndpointMeta meta;
        for (Method m : cls.getDeclaredMethods()) {
            meta = EndpointMeta.of(m);
            if (meta == null) continue;

            Map<HTTPVerb, Registration> sub = this.map.get(meta.path());
            if (sub == null) {
                sub = new EnumMap<>(HTTPVerb.class);
                this.map.put(meta.path(), sub);
            }

            sub.put(meta.verb(), new Registration(endpoints, meta, m));
        }
    }

    //

    private record Registration(
            @NotNull Endpoints registrar,
            @NotNull EndpointMeta meta,
            @NotNull Method method
    ) { }

    private interface VerbLike {

        boolean isMeta();

        @Nullable HTTPVerb toVerb();

        @NotNull String toString();

        //

        record Literal(@NotNull HTTPVerb handle) implements VerbLike {

            @Override
            public boolean isMeta() {
                return false;
            }

            @Override
            public @NotNull HTTPVerb toVerb() {
                return this.handle;
            }

            @Override
            public @NotNull String toString() {
                return this.handle.name();
            }

        }

        record Arbitrary(@NotNull String text) implements VerbLike {

            @Override
            public boolean isMeta() {
                return this.text.equals("HEAD") || this.text.equals("OPTIONS");
            }

            @Override
            public @Nullable HTTPVerb toVerb() {
                try {
                    return HTTPVerb.valueOf(this.text);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }

            @Override
            public @NotNull String toString() {
                return this.text;
            }

        }

    }

}
