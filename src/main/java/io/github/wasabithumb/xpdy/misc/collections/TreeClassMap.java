package io.github.wasabithumb.xpdy.misc.collections;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.TreeMap;

@ApiStatus.Internal
final class TreeClassMap<C, V> extends TreeMap<Class<? extends C>, V> implements ClassMap<C, V> {

    private static int hierarchicalDepth(@NotNull Class<?> cls) {
        if (cls.isPrimitive() || cls.isInterface()) return 0;
        int depth = 0;
        do {
            depth++;
            cls = cls.getSuperclass();
        } while (cls != null);
        return depth;
    }

    private static int compare(@NotNull Class<?> a, @NotNull Class<?> b) {
        int cmp = Integer.compare(hierarchicalDepth(b), hierarchicalDepth(a));
        if (cmp == 0) cmp = CharSequence.compare(b.getName(), a.getName());
        return cmp;
    }

    //

    public TreeClassMap() {
        super(TreeClassMap::compare);
    }

}
