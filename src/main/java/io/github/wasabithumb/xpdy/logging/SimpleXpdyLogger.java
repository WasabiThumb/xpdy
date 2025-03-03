package io.github.wasabithumb.xpdy.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.PrintStream;

final class SimpleXpdyLogger implements XpdyLogger {

    public static final SimpleXpdyLogger INSTANCE = new SimpleXpdyLogger();

    @Override
    public void log(
            @Range(from = 100, to = 599) int responseCode,
            @NotNull String method,
            @NotNull String path,
            @Nullable Throwable exception
    ) {
        String message = "[" + responseCode + "] " + method + " " + path;
        PrintStream ps = (responseCode < 400) ? System.out : System.err;
        if (exception != null) {
            exception.printStackTrace(ps);
        }
        ps.println(message);
    }

}
