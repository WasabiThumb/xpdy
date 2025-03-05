package io.github.wasabithumb.xpdy;

import com.sun.net.httpserver.*;
import io.github.wasabithumb.xpdy.endpoint.EndpointInjector;
import io.github.wasabithumb.xpdy.endpoint.EndpointRegistry;
import io.github.wasabithumb.xpdy.endpoint.Endpoints;
import io.github.wasabithumb.xpdy.except.handling.ExceptionHandler;
import io.github.wasabithumb.xpdy.logging.XpdyLogger;
import io.github.wasabithumb.xpdy.misc.MimeType;
import io.github.wasabithumb.xpdy.misc.MimeTypes;
import io.github.wasabithumb.xpdy.nd.StaticContent;
import io.github.wasabithumb.xpdy.payload.body.Body;
import io.github.wasabithumb.xpdy.payload.request.Request;
import io.github.wasabithumb.xpdy.payload.response.Response;
import org.jetbrains.annotations.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

@ApiStatus.Internal
final class XpdyServerImpl extends EndpointRegistry implements XpdyServer {

    static final String VERSION = Objects.requireNonNullElse(XpdyBuildInfo.get("Version"), "unknown");
    static final String IDENTIFIER = "xpdy/" +
            VERSION +
            " (" + System.getProperty("os.name") + ")";

    //

    private final InetSocketAddress address;
    private final String name;
    private final EndpointInjector injector;
    private final StaticContent staticContent;
    private final HttpsConfigurator httpsConfigurator;
    private final Executor executor;
    private HttpServer handle = null;

    XpdyServerImpl(
            @NotNull InetSocketAddress address,
            @NotNull String name,
            @NotNull EndpointInjector injector,
            @NotNull StaticContent staticContent,
            @NotNull XpdyLogger logger,
            @NotNull @MimeType String defaultIn,
            @NotNull @MimeType String defaultOut,
            @NotNull ExceptionHandler exceptionHandler,
            @Nullable HttpsConfigurator httpsConfigurator,
            @Nullable Executor executor
    ) {
        super(logger, defaultIn, defaultOut, exceptionHandler);
        this.address = address;
        this.name = name;
        this.injector = injector;
        this.staticContent = staticContent;
        this.httpsConfigurator = httpsConfigurator;
        this.executor = executor;
    }

    //

    @Override
    public void registerEndpoints(@NotNull Class<? extends Endpoints> clazz) {
        this.registerEndpoints(this.injector.inject(clazz));
    }

    @Override
    public synchronized void start() throws IOException {
        if (this.handle != null) return;

        HttpServer server;
        if (this.httpsConfigurator != null) {
            HttpsServer s = HttpsServer.create();
            s.setHttpsConfigurator(this.httpsConfigurator);
            server = s;
        } else {
            server = HttpServer.create();
        }

        if (this.executor != null) {
            server.setExecutor(this.executor);
        }

        server.bind(this.address, 0);
        server.createContext("/", this::handle);
        server.start();

        this.handle = server;
    }

    @Override
    public synchronized void stop(int delay) {
        if (this.handle == null) return;
        this.handle.stop(delay);
        this.handle = null;
    }

    private void handle(@NotNull HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        Response response = this.invokeStatic(method, path);
        if (response == null) {
            response = this.invokeEndpoint(
                    exchange.getRequestMethod(),
                    path,
                    (List<String> params) -> Request.of(exchange, params)
            );
        }

        Headers headers = exchange.getResponseHeaders();
        headers.set("Server", this.name);
        headers.putAll(response.headers());
        response.cookies().write(headers);

        Body body = response.body();
        long size = body.size();
        exchange.sendResponseHeaders(response.code(), size == -1L ? 0L : (size == 0L ? -1L : size));

        if (size == 0L) return;

        try (OutputStream os = exchange.getResponseBody()) {
            body.pipe(os);
            os.flush();
        }
    }

    private @Nullable Response invokeStatic(@NotNull String method, @NotNull String path) throws IOException {
        Body body;
        try {
            //noinspection PatternValidation
            body = this.staticContent.serve(path);
        } catch (IOException e) {
            return Response.error(502);
        }
        if (body == null) return null;

        if (method.equalsIgnoreCase("HEAD") || method.equalsIgnoreCase("OPTIONS")) {
            body.pipe(OutputStream.nullOutputStream());
            return Response.builder()
                    .code(204)
                    .setHeader("Allow", "GET, HEAD, OPTIONS")
                    .setHeader("Content-Type", body.type())
                    .setHeader("Content-Length", Long.toString(body.size()))
                    .build();
        } else if (!method.equalsIgnoreCase("GET")) {
            body.pipe(OutputStream.nullOutputStream());
            return Response.error(405);
        }

        return Response.of(body);
    }

    //

    public static final class Builder implements XpdyServer.Builder {

        private boolean           open              = true;
        private InetAddress       address           = InetAddress.getLoopbackAddress();
        private int               port              = 9739;
        private XpdyLogger        logger            = XpdyLogger.global();
        private String            name              = IDENTIFIER;
        private String            defaultIn         = MimeTypes.URLENCODED;
        private String            defaultOut        = MimeTypes.HTML;
        private EndpointInjector  injector          = new EndpointInjector();
        private StaticContent     staticContent     = StaticContent.empty();
        private ExceptionHandler  exceptionHandler  = ExceptionHandler.DEFAULT;
        private HttpsConfigurator httpsConfigurator = null;
        private Executor          executor          = null;

        //

        private synchronized void checkOpen() {
            if (!this.open)
                throw new IllegalStateException("Cannot use Builder after #build()");
        }

        private synchronized void close() {
            this.checkOpen();
            this.open = false;
        }

        //

        @Override
        @Contract("_ -> this")
        public @NotNull Builder address(@NotNull InetAddress address) {
            this.checkOpen();
            this.address = address;
            return this;
        }

        @Override
        @Contract("_ -> this")
        public @NotNull Builder port(@Range(from=0, to=65535) int port) {
            this.checkOpen();
            this.port = port;
            return this;
        }

        @Override
        @Contract("_ -> this")
        public @NotNull Builder logger(@Nullable XpdyLogger logger) {
            this.checkOpen();
            this.logger = (logger == null) ? XpdyLogger.silent() : logger;
            return this;
        }

        @Override
        @Contract("_ -> this")
        public @NotNull Builder name(@NotNull String name) {
            this.checkOpen();
            this.name = name;
            return this;
        }

        @Override
        @Contract("_ -> this")
        public @NotNull Builder defaultIn(@NotNull @MimeType String type) {
            this.checkOpen();
            this.defaultIn = type;
            return this;
        }

        @Override
        @Contract("_ -> this")
        public @NotNull Builder defaultOut(@NotNull @MimeType String type) {
            this.checkOpen();
            this.defaultOut = type;
            return this;
        }

        @Override
        @Contract("_, _ -> this")
        public <T> @NotNull Builder inject(@NotNull Class<T> type, @NotNull T value) {
            this.checkOpen();
            this.injector.register(type, value);
            return this;
        }

        @Override
        @Contract("_ -> this")
        public @NotNull Builder exceptionHandler(@NotNull ExceptionHandler exceptionHandler) {
            this.checkOpen();
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        @Override
        @Contract("_ -> this")
        public @NotNull Builder httpsConfigurator(@Nullable HttpsConfigurator httpsConfigurator) {
            this.checkOpen();
            this.httpsConfigurator = httpsConfigurator;
            return this;
        }

        @Override
        @Contract("_ -> this")
        public @NotNull Builder executor(@Nullable Executor executor) {
            this.checkOpen();
            this.executor = executor;
            return this;
        }

        @Override
        @Contract("_ -> this")
        public @NotNull XpdyServer.Builder staticContent(@NotNull StaticContent staticContent) {
            this.checkOpen();
            this.staticContent = staticContent;
            return this;
        }

        //

        @Override
        @Contract("-> new")
        public @NotNull XpdyServer build() {
            this.close();
            XpdyServerImpl ret = new XpdyServerImpl(
                    new InetSocketAddress(this.address, this.port),
                    this.name,
                    this.injector,
                    this.staticContent,
                    this.logger,
                    this.defaultIn,
                    this.defaultOut,
                    this.exceptionHandler,
                    this.httpsConfigurator,
                    this.executor
            );
            this.injector.register(XpdyServer.class, ret);
            return ret;
        }

    }

}
