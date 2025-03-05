package io.github.wasabithumb.xpdy;

import com.sun.net.httpserver.HttpsConfigurator;
import io.github.wasabithumb.xpdy.endpoint.Endpoints;
import io.github.wasabithumb.xpdy.except.handling.ExceptionHandler;
import io.github.wasabithumb.xpdy.logging.XpdyLogger;
import io.github.wasabithumb.xpdy.misc.MimeType;
import io.github.wasabithumb.xpdy.nd.StaticContent;
import org.jetbrains.annotations.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

public interface XpdyServer {

    /**
     * Reports the loaded version string in the form {@code major.minor.patch}
     * @since 0.2.0
     */
    static @NotNull String version() {
        return XpdyServerImpl.VERSION;
    }

    /**
     * Starts building a server instance.
     */
    @Contract("-> new")
    static @NotNull Builder builder() {
        return new XpdyServerImpl.Builder();
    }

    //

    /**
     * <p>
     *     Registers the endpoints contained in the specified
     *     {@link Endpoints endpoint class}.
     * </p>
     * <p>
     *     If there is a constructor marked with {@link io.github.wasabithumb.xpdy.endpoint.EndpointInject @EndpointInject},
     *     that constructor is used to create an instance of the class. Otherwise, the primary (no args) constructor is
     *     used. Any fields marked with {@link io.github.wasabithumb.xpdy.endpoint.EndpointInject @EndpointInject}
     *     will also be populated when possible.
     * </p>
     * <p>
     *     By default, only {@link XpdyServer} is injectable.
     *     Other injections can be registered with {@link XpdyServer.Builder#inject(Class, Object) Builder#inject}.
     * </p>
     */
    void registerEndpoints(@NotNull Class<? extends Endpoints> clazz);

    /**
     * Registers the {@link io.github.wasabithumb.xpdy.endpoint.methods endpoints} contained in the specified
     * endpoint classes.
     * @see #registerEndpoints(Class)
     */
    @SuppressWarnings("unchecked")
    default void registerEndpoints(@NotNull Class<? extends Endpoints> @NotNull ... classes) {
        for (Class<?> clazz : classes) {
            this.registerEndpoints(clazz.asSubclass(Endpoints.class));
        }
    }

    /**
     * Starts the server
     * @throws IOException I/O error
     * @throws java.net.BindException Could not bind to the specified address & port
     */
    void start() throws IOException;

    /**
     * Stops the server as specified
     * by {@link com.sun.net.httpserver.HttpServer#stop(int) HttpServer#stop(int)}
     */
    void stop(int delay);

    //

    interface Builder {

        /**
         * Sets the address to bind to. By default, the
         * {@link InetAddress#getLoopbackAddress() loopback address} is used.
         */
        @Contract("_ -> this")
        @NotNull Builder address(@NotNull InetAddress address);

        /**
         * Sets the port to bind to. Default is {@code 9739}.
         */
        @Contract("_ -> this")
        @NotNull Builder port(@Range(from=0, to=65535) int port);

        /**
         * Sets the logger to use. If null, server is silenced.
         * @see #logger(Logger)
         */
        @Contract("_ -> this")
        @NotNull Builder logger(@Nullable XpdyLogger logger);

        /**
         * Sets the logger to use. If null, server is silenced.
         * @see #logger(XpdyLogger)
         */
        @Contract("_ -> this")
        default @NotNull Builder logger(@Nullable Logger logger) {
            return this.logger(logger == null ? null : XpdyLogger.of(logger));
        }

        /**
         * Sets the server name (value of the
         * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Server">Server</a> header).
         * Default is {@code xpdy/$VERSION ($OS)}
         */
        @Contract("_ -> this")
        @NotNull Builder name(@NotNull String name);

        /**
         * Sets the default in type (value of the
         * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept">Accept</a> header).
         * This can be overridden at the endpoint level.
         */
        @Contract("_ -> this")
        @NotNull Builder defaultIn(@NotNull @MimeType String type);

        /**
         * Sets the default out type (value of the
         * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type">Content-Type</a> header).
         * This can be overridden at the endpoint level.
         */
        @Contract("_ -> this")
        @NotNull Builder defaultOut(@NotNull @MimeType String type);

        /**
         * Registers an injection.
         * If a field or constructor parameter marked with
         * {@link io.github.wasabithumb.xpdy.endpoint.EndpointInject @EndpointInject}
         * is present in an {@link Endpoints endpoint class}, it will be injected with the
         * registered value of matching type.
         */
        @Contract("_, _ -> this")
        <T> @NotNull Builder inject(@NotNull Class<T> type, @NotNull T value);

        /**
         * Sets the {@link ExceptionHandler exception handler} to use.
         * This maps JVM exceptions to HTTP response codes.
         * Default is {@link ExceptionHandler#DEFAULT}.
         */
        @Contract("_ -> this")
        @NotNull Builder exceptionHandler(@NotNull ExceptionHandler exceptionHandler);

        /**
         * <p>
         *     Sets the {@link HttpsConfigurator}, as specified by
         *     {@link com.sun.net.httpserver.HttpsServer#setHttpsConfigurator(HttpsConfigurator) HttpsServer}.
         * </p>
         * <p>
         *     If non-null, the underlying server will be a {@link com.sun.net.httpserver.HttpsServer HttpsServer}.
         * </p>
         * <p>
         *     If null, the underlying server will be a {@link com.sun.net.httpserver.HttpServer HttpServer}.
         * </p>
         */
        @Contract("_ -> this")
        @NotNull Builder httpsConfigurator(@Nullable HttpsConfigurator httpsConfigurator);

        /**
         * Sets the executor to use, as specified by
         * {@link com.sun.net.httpserver.HttpServer#setExecutor(Executor) HttpServer}.
         */
        @Contract("_ -> this")
        @NotNull Builder executor(@Nullable Executor executor);

        /**
         * Sets the {@link StaticContent static content} to serve at the root path.
         * @since 0.2.0
         */
        @ApiStatus.AvailableSince("0.2.0")
        @Contract("_ -> this")
        @NotNull Builder staticContent(@NotNull StaticContent staticContent);

        /**
         * Builds a server instance.
         * Server is not started automatically, use {@link XpdyServer#start()} to start.
         */
        @Contract("-> new")
        @NotNull
        XpdyServer build();

    }

}
