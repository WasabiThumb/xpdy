package io.github.wasabithumb.xpdy.except.handling;

import io.github.wasabithumb.xpdy.except.ServeException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.function.ToIntFunction;

/**
 * Responsible for converting exceptions which bubble out of endpoint methods into
 * HTTP error codes.
 */
@FunctionalInterface
public interface ExceptionHandler {

    @Contract("-> new")
    static @NotNull Builder builder() {
        return new ExceptionHandlerImpl.Builder();
    }

    /**
     * An exception handler with the default rules,
     * as specified by {@link Builder#handleDefaults() Builder#handleDefaults}.
     */
    ExceptionHandler DEFAULT = builder()
            .handleDefaults()
            .build();

    //

    /**
     * Provides the appropriate HTTP error response code for the given error.
     * Should default to 500 (Internal Server Error) if not otherwise handled.
     */
    @Range(from=400, to=599) int handle(@NotNull Throwable t);

    //

    interface Builder {

        /**
         * Adds a handler for the given exception class. When an exception matches multiple handlers,
         * the more specific handler takes priority.
         * @param clazz Handler will match any exception that extends this class
         * @param generator Will be called for matching exceptions to determine the HTTP error response code they
         *                  correspond to. Should be in the range 400 - 599.
         */
        @Contract("_, _ -> this")
        <T extends Throwable> @NotNull Builder handle(
                @NotNull Class<T> clazz,
                @NotNull ToIntFunction<T> generator
        );

        /**
         * Adds a handler for the given exception class.
         * @param clazz Handler will match any exception that extends this class
         * @param responseCode The corresponding HTTP error response code
         * @see #handle(Class, ToIntFunction)
         */
        @Contract("_, _ -> this")
        default <T extends Throwable> @NotNull Builder handle(
                @NotNull Class<T> clazz,
                @Range(from=400, to=599) int responseCode
        ) {
            return this.handle(clazz, (T ignored) -> responseCode);
        }

        /**
         * Adds the following handlers:
         * <ul>
         *     <li>{@link ServeException} -> HTTP 400 - 5XX</li>
         *     <li>{@link NumberFormatException} -> HTTP 400 (Bad Request)</li>
         *     <li>{@link ParseException} -> HTTP 400 (Bad Request)</li>
         *     <li>{@link SQLException} -> HTTP 502 (Bad Gateway)</li>
         * </ul>
         * Other exceptions are not affected (will resolve to HTTP 500 unless otherwise specified).
         * @see #handle(Class, ToIntFunction)
         */
        @Contract("-> this")
        default @NotNull Builder handleDefaults() {
            return this.handle(ServeException.class, ServeException::responseCode)
                    .handle(NumberFormatException.class, 400)
                    .handle(ParseException.class, 400)
                    .handle(SQLException.class, 502);
        }

        @Contract("-> new")
        @NotNull ExceptionHandler build();

    }

}
