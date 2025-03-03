package io.github.wasabithumb.xpdy.payload.request;

import com.sun.net.httpserver.HttpExchange;
import io.github.wasabithumb.xpdy.except.BodyFormatException;
import io.github.wasabithumb.xpdy.misc.collections.UnionMap;
import io.github.wasabithumb.xpdy.payload.Payload;
import io.github.wasabithumb.xpdy.payload.body.Body;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@ApiStatus.NonExtendable
public interface Request extends Payload {

    static @NotNull Request of(@NotNull HttpExchange exchange, @NotNull List<String> pathParameters) {
        return new ExchangeRequest(exchange, pathParameters);
    }

    //

    /**
     * The parsed (URL decoded) query string.
     */
    @NotNull @Unmodifiable Map<String, String> query();

    /**
     * A combination of the {@link #query() query string} and {@link Body#urlencoded() body};
     * asserting that the body is either empty or in urlencoded format.
     * If a key is present in both the query string and body, the query string takes priority.
     */
    default @NotNull @Unmodifiable Map<String, String> urlencoded() throws IOException, BodyFormatException {
        return new UnionMap<>(
                this.query(),
                this.body().urlencoded()
        );
    }

    /**
     * <p>
     *     If this request object is serving an endpoint with a wildcard path,
     *     this list contains the actual value of each wildcard in order.
     * </p>
     * <h2>Examples</h2>
     * <table>
     *     <tr>
     *         <th>Endpoint Path</th>
     *         <th>Request Path</th>
     *         <th>Parameters</th>
     *     </tr>
     *     <tr>
     *         <td>/sample</td>
     *         <td>/sample</td>
     *         <td>{@code []}</td>
     *     </tr>
     *     <tr>
     *         <td>/foo/*</td>
     *         <td>/foo/bar</td>
     *         <td>{@code ["bar"]}</td>
     *     </tr>
     *     <tr>
     *         <td>/foo/{@literal *}/*</td>
     *         <td>/foo/bar/baz</td>
     *         <td>{@code ["bar", "baz"]}</td>
     *     </tr>
     *     <tr>
     *         <td>/a/{@literal *}/c/*</td>
     *         <td>/a/b/c/d</td>
     *         <td>{@code ["b", "d"]}</td>
     *     </tr>
     * </table>
     */
    @NotNull @Unmodifiable List<String> pathParameters();

}
