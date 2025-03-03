package io.github.wasabithumb.xpdy.except;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exception that may be thrown by body parsing methods to indicate that the data is malformed.
 * When this exception bubbles out of endpoint methods, HTTP 400 is served.
 */
public class BodyFormatException extends ServeException {

    public BodyFormatException(@NotNull String message) {
        super(400, message);
    }

    public BodyFormatException(@NotNull String message, @Nullable Throwable cause) {
        super(400, message, cause);
    }

}
