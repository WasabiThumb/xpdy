package io.github.wasabithumb.xpdy.misc.collections;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ApiStatus.Internal
final class UnmodifiableClassMap<C, V> extends AbstractMap<Class<? extends C>, V> implements ClassMap<C, V> {

    private final SortedMap<Class<? extends C>, V> backing;

    UnmodifiableClassMap(
            @NotNull SortedMap<Class<? extends C>, V> backing
    ) {
        this.backing = Collections.unmodifiableSortedMap(backing);
    }

    //

    @Override
    public @Nullable Comparator<? super Class<? extends C>> comparator() {
        return this.backing.comparator();
    }

    @Override
    public @NotNull SortedMap<Class<? extends C>, V> subMap(Class<? extends C> aClass, Class<? extends C> k1) {
        return this.backing.subMap(aClass, k1);
    }

    @Override
    public @NotNull SortedMap<Class<? extends C>, V> headMap(Class<? extends C> aClass) {
        return this.backing.headMap(aClass);
    }

    @Override
    public @NotNull SortedMap<Class<? extends C>, V> tailMap(Class<? extends C> aClass) {
        return this.backing.tailMap(aClass);
    }

    @Override
    public @NotNull Class<? extends C> firstKey() {
        return this.backing.firstKey();
    }

    @Override
    public @NotNull Class<? extends C> lastKey() {
        return this.backing.lastKey();
    }

    @Override
    public @NotNull Set<Entry<Class<? extends C>, V>> entrySet() {
        return this.backing.entrySet();
    }

}
