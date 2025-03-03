package io.github.wasabithumb.xpdy.payload;

import com.sun.net.httpserver.Headers;
import io.github.wasabithumb.xpdy.payload.body.Body;
import io.github.wasabithumb.xpdy.payload.cookie.Cookies;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface Payload {

    /**
     * Body of the payload. This is the data sent/received.
     */
    @Contract(pure = true)
    @NotNull Body body();

    /**
     * HTTP headers attached to the payload. This will be mutable for outgoing payloads (responses).
     * This should not be used to add {@code Set-Cookie} headers, for that use {@link #cookies()}.
     */
    @Contract(pure = true)
    @NotNull Headers headers();

    /**
     * HTTP cookies; implied by the {@link #headers() headers} or initially empty.
     * This will be mutable for outgoing payloads (responses).
     */
    @Contract(pure = true)
    @NotNull Cookies cookies();

}
