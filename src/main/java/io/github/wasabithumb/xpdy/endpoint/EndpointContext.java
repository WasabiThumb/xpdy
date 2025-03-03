package io.github.wasabithumb.xpdy.endpoint;

import io.github.wasabithumb.xpdy.misc.collections.ClassMap;
import io.github.wasabithumb.xpdy.payload.request.Request;
import io.github.wasabithumb.xpdy.payload.response.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Map;

/**
 * A registry for values related to an endpoint invocation.
 * {@link io.github.wasabithumb.xpdy.payload.request.Request Request} and
 * {@link EndpointMeta EndpointMeta} are guaranteed to be initially set.
 * {@link io.github.wasabithumb.xpdy.payload.response.Response Response} will be set after
 * the invocation and available in {@link Endpoints#afterEach(EndpointContext) Endpoints#afterEach}.
 */
public final class EndpointContext {

    private final ClassMap<Object, Object> map = ClassMap.create();

    EndpointContext(
            @NotNull Request request,
            @NotNull EndpointMeta meta
    ) {
        this.map.put(EndpointContext.class, this);
        this.map.put(Request.class, request);
        this.map.put(EndpointMeta.class, meta);
    }

    //

    public <T> @UnknownNullability T get(@NotNull Class<T> clazz) {
        Object ret = this.map.get(clazz);
        if (ret != null) return clazz.cast(ret);

        for (Map.Entry<Class<?>, Object> entry : this.map.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey()))
                return clazz.cast(entry.getValue());
        }

        return null;
    }

    public <T> void set(@NotNull Class<T> clazz, @NotNull T value) {
        this.map.put(clazz, value);
    }

    //

    public @NotNull Request getRequest() {
        return this.get(Request.class);
    }

    public void setRequest(@NotNull Request request) {
        this.set(Request.class, request);
    }

    public @NotNull EndpointMeta getMeta() {
        return this.get(EndpointMeta.class);
    }

    public @NotNull Response getResponse() throws IllegalStateException {
        Response response = this.get(Response.class);
        if (response == null)
            throw new IllegalStateException("Response is not set");
        return response;
    }

    public void setResponse(@NotNull Response response) {
        this.set(Response.class, response);
    }

    //

    @Override
    public @NotNull String toString() {
        return this.map.toString();
    }

}
