package io.github.wasabithumb.xpdy.misc.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A {@link java.util.function.Supplier Supplier}-like functional interface which may throw {@link IOException}
 */
@FunctionalInterface
public interface IOSupplier<T> {

    @NotNull T execute() throws IOException;

}
