package io.github.wasabithumb.xpdy.logging;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Internal
record StandardXpdyLogger(
        @NotNull Logger handle
) implements XpdyLogger {

    public static XpdyLogger GLOBAL = new StandardXpdyLogger(Logger.getLogger("xpdy"));

    @Override
    public void log(
            @Range(from = 100, to = 599) int responseCode,
            @NotNull String method,
            @NotNull String path,
            @Nullable Throwable exception
    ) {
        Level level;
        if (responseCode < 400) {
            level = Level.INFO;
        } else if (responseCode < 500) {
            level = Level.WARNING;
        } else {
            level = (exception != null) ? Level.SEVERE : Level.WARNING;
        }

        String msg = "[" + responseCode + "] " + method + " " + path;

        if (exception == null) {
            this.handle.log(level, msg);
        } else {
            this.handle.log(level, msg, exception);
        }
    }

}
