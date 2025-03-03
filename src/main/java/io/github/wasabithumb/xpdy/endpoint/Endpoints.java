package io.github.wasabithumb.xpdy.endpoint;

import io.github.wasabithumb.xpdy.payload.request.Request;
import io.github.wasabithumb.xpdy.payload.response.Response;
import org.jetbrains.annotations.NotNull;

/**
 * A marker interface for classes that serve endpoints.
 * Endpoint methods must be annotated with an
 * {@link io.github.wasabithumb.xpdy.endpoint.methods appropriate annotation}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * class EchoEndpoints implements Endpoints {
 *
 *      @EndpointInject
 *      private XpdyServer server;
 *
 *      @Post("/api/echo")
 *      Response echo(Request request) {
 *          return Response.builder()
 *              .body(request.body())
 *              .build();
 *      }
 *
 * }
 * }</pre>
 * @see #beforeEach(EndpointContext)
 * @see #afterEach(EndpointContext)
 */
public interface Endpoints {

    /**
     * Optional code to run before each endpoint handler
     */
    default void beforeEach(
            @NotNull EndpointContext ctx
    ) throws Exception { }

    /**
     * Optional code to run after each endpoint handler
     */
    default void afterEach(
            @NotNull EndpointContext ctx
    ) throws Exception { }

}
