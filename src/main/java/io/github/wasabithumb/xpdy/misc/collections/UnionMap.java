package io.github.wasabithumb.xpdy.misc.collections;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * A hacky unmodifiable map which uses the union of the keys of 2
 * source maps to expose 1 continuous map.
 */
@ApiStatus.Internal
public class UnionMap<K, V> extends AbstractMap<K, V> {

    private final Map<K, ? extends V> primary;
    private final Map<K, ? extends V> secondary;
    private final Set<K> keys;

    public UnionMap(
            @NotNull Map<K, ? extends V> primary,
            @NotNull Map<K, ? extends V> secondary
    ) {
        this.primary = primary;
        this.secondary = secondary;

        Set<K> a = primary.keySet();
        Set<K> b = secondary.keySet();

        if (a.isEmpty()) {
            this.keys = b;
        } else if (b.isEmpty()) {
            this.keys = a;
        } else {
            Set<K> c = new HashSet<>(a.size() + b.size());
            c.addAll(a);
            c.addAll(b);
            this.keys = Collections.unmodifiableSet(c);
        }
    }

    //

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return this.keys;
    }

    @Override
    public boolean containsKey(Object key) {
        //noinspection SuspiciousMethodCalls
        return this.keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.primary.containsValue(value) || this.secondary.containsValue(value);
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return new EntrySet<>(this);
    }

    @Override
    public V get(Object key) {
        V ret = this.primary.get(key);
        if (ret == null) ret = this.secondary.get(key);
        return ret;
    }

    //

    private static final class EntrySet<A, B> extends AbstractSet<Map.Entry<A, B>> {

        private final UnionMap<A, B> parent;
        EntrySet(@NotNull UnionMap<A, B> parent) {
            this.parent = parent;
        }

        //

        @Override
        public int size() {
            return this.parent.keys.size();
        }

        @Contract(" -> new")
        @Override
        public @NotNull Iterator<Entry<A, B>> iterator() {
            return new EntryIter<>(this.parent, this.parent.keys.iterator());
        }

    }

    //

    private static final class EntryIter<A, B> implements Iterator<Map.Entry<A, B>> {

        private final UnionMap<A, B> parent;
        private final Iterator<A> handle;
        EntryIter(
                @NotNull UnionMap<A, B> parent,
                @NotNull Iterator<A> handle
        ) {
            this.parent = parent;
            this.handle = handle;
        }

        //

        @Override
        public boolean hasNext() {
            return this.handle.hasNext();
        }

        @Override
        public @NotNull @Unmodifiable Entry<A, B> next() {
            A a = this.handle.next();
            return Map.entry(a, this.parent.get(a));
        }

    }

}
