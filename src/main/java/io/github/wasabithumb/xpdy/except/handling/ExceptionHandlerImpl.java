package io.github.wasabithumb.xpdy.except.handling;

import io.github.wasabithumb.xpdy.misc.collections.ClassMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.function.ToIntFunction;

@ApiStatus.Internal
final class ExceptionHandlerImpl implements ExceptionHandler {

    private final ClassMap<Throwable, ? extends ToIntFunction<Throwable>> map;
    private ExceptionHandlerImpl(
            @NotNull ClassMap<Throwable, ? extends ToIntFunction<Throwable>> map
    ) {
        this.map = map;
    }

    //

    @Override
    public @Range(from=400, to=599) int handle(@NotNull Throwable t) {
        for (Map.Entry<Class<? extends Throwable>, ? extends ToIntFunction<Throwable>> entry : this.map.entrySet()) {
            if (entry.getKey().isInstance(t)) return entry.getValue().applyAsInt(t);
        }
        return 500;
    }

    //

    static final class Builder implements ExceptionHandler.Builder {

        private final ClassMap<Throwable, Handler<?>> map = ClassMap.create();

        //

        @Override
        public <T extends Throwable> @NotNull Builder handle(
                @NotNull Class<T> clazz,
                @NotNull ToIntFunction<T> generator
        ) {
            this.map.put(clazz, new Handler<>(clazz, generator));
            return this;
        }

        @Override
        public @NotNull ExceptionHandler build() {
            return new ExceptionHandlerImpl(ClassMap.copyOf(this.map));
        }

        //

        private record Handler<T>(
                @NotNull Class<? extends T> exceptionClass,
                @NotNull ToIntFunction<T> typedHandler
        ) implements ToIntFunction<Throwable> {

            @Override
            public int applyAsInt(@NotNull Throwable throwable) {
                return this.typedHandler.applyAsInt(this.exceptionClass.cast(throwable));
            }

        }

    }

}
