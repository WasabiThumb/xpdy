package io.github.wasabithumb.xpdy.except;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown by {@link io.github.wasabithumb.xpdy.payload.cookie.Cookies Cookies} when
 * trying to read a cookie that the client did not provide.
 * When this exception bubbles out of endpoint methods, HTTP 400 is served.
 */
public class MissingCookieException extends ServeException {

    public MissingCookieException(@NotNull String cookieName) {
        super(400, "Missing cookie \"" + cookieName + "\"");
    }

}
