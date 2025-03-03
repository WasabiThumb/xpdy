package io.github.wasabithumb.xpdy.logging;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@ApiStatus.Internal
final class SilentXpdyLogger implements XpdyLogger {

    static final SilentXpdyLogger INSTANCE = new SilentXpdyLogger();

    @Override
    public void log(
            @Range(from = 100, to = 599) int responseCode,
            @NotNull String method,
            @NotNull String path,
            @Nullable Throwable exception
    ) {
    }

}
