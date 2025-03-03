package io.github.wasabithumb.xpdy.misc.collections;

import org.jetbrains.annotations.*;

import java.util.Map;
import java.util.SortedMap;

/**
 * <p>
 *     A sorted map with {@link Class Class} keys of a given supertype.
 *     Entries are sorted in a useful way; first by class hierarchical depth and second by class name.
 * </p>
 * <p>
 *     Hierarchical depth is here defined by the following expression:
 *     <pre>{@code
 *     hd(class) = class ? 1 + hd(class->superclass) : 0
 *     }</pre>
 *     For instance, {@link Object Object.class} has a hierarchical depth of 1
 *     and {@link Integer Integer.class} has a hierarchical depth of 3. This means that
 *     {@link Integer Class&lt;Integer&gt;} entries will always be placed <strong>behind</strong>
 *     {@link Object Class&lt;Object&gt;} entries in the map.
 *     Having entries sorted by "decreasing specificity" in the iterator allows for efficient
 *     and intuitive behavior for configured mappings.
 * </p>
 */
@ApiStatus.Internal
public interface ClassMap<C, V> extends SortedMap<Class<? extends C>, V> {

    @Contract("-> new")
    static <CC, VV> @NotNull ClassMap<CC, VV> create() {
        return new TreeClassMap<>();
    }

    static <CC, VV> @NotNull @UnmodifiableView ClassMap<CC, VV> unmodifiableView(@NotNull ClassMap<CC, VV> map) {
        if (map instanceof UnmodifiableClassMap<CC, VV> u) return u;
        return new UnmodifiableClassMap<>(map);
    }

    @Contract("_ -> new")
    static <CC, VV> @NotNull ClassMap<CC, VV> mutableCopyOf(@NotNull Map<? extends Class<? extends CC>, ? extends VV> map) {
        ClassMap<CC, VV> ret = create();
        ret.putAll(map);
        return ret;
    }

    @Contract("_ -> new")
    static <CC, VV> @NotNull @Unmodifiable ClassMap<CC, VV> copyOf(@NotNull Map<? extends Class<? extends CC>, ? extends VV> map) {
        return unmodifiableView(mutableCopyOf(map));
    }

}
