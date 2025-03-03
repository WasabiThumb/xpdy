package io.github.wasabithumb.xpdy.logging;

import io.github.wasabithumb.xpdy.XpdyServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.logging.Logger;

/**
 * A logger specialized for the {@link XpdyServer XpdyServer}.
 * @see #of(Logger)
 */
@FunctionalInterface
public interface XpdyLogger {

    /**
     * Returns a {@link XpdyLogger} which ignores all log events
     */
    static @NotNull XpdyLogger silent() {
        return SilentXpdyLogger.INSTANCE;
    }

    /**
     * Wraps a standard {@link Logger} into a new {@link XpdyLogger}
     */
    @Contract("_ -> new")
    static @NotNull XpdyLogger of(@NotNull Logger logger) {
        return new StandardXpdyLogger(logger);
    }

    /**
     * Alias for {@code of(Logger.getLogger("xpdy"))}
     * @see #of(Logger)
     */
    static @NotNull XpdyLogger global() {
        return StandardXpdyLogger.GLOBAL;
    }

    /**
     * Returns a logger which writes directly to {@link System#out} and {@link System#err}.
     * Mostly for debug/testing purposes.
     */
    static @NotNull XpdyLogger simple() {
        return SimpleXpdyLogger.INSTANCE;
    }

    //

    void log(@Range(from=100, to=599) int responseCode, @NotNull String method, @NotNull String path, @Nullable Throwable exception);

}
