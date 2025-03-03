package io.github.wasabithumb.xpdy.misc.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A {@link java.util.function.Consumer Consumer}-like functional interface which may throw {@link IOException}
 */
@FunctionalInterface
public interface IOConsumer<T> {

    void execute(@NotNull T t) throws IOException;

}
