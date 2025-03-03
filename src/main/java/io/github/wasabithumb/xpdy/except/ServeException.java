package io.github.wasabithumb.xpdy.except;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Unchecked exception that causes HTTP 4XX or 5XX to be served when
 * thrown in endpoint methods.
 */
public class ServeException extends RuntimeException {

    private final int responseCode;

    //

    public ServeException(
            @Range(from=400, to=599) int responseCode
    ) {
        super();
        this.responseCode = responseCode;
    }

    public ServeException(
            @Range(from=400, to=599) int responseCode,
            @NotNull String message
    ) {
        super(message);
        this.responseCode = responseCode;
    }

    public ServeException(
            @Range(from=400, to=599) int responseCode,
            @NotNull String message,
            @Nullable Throwable cause
    ) {
        super(message, cause);
        this.responseCode = responseCode;
    }

    //

    public final @Range(from=400, to=599) int responseCode() {
        return this.responseCode;
    }

}
