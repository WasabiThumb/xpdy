package io.github.wasabithumb.xpdy.misc.path;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.CharBuffer;
import java.util.*;

/**
 * An intrinsically sorted map where the keys are URI paths.
 * If a path part is an asterisk ({@code *}), it acts as a wildcard.
 */
@ApiStatus.Internal
public final class PathMap<T> {

    private final PathMapNode<T> root = new PathMapNode<>();

    //

    private @NotNull Queue<CharSequence> split(@NotNull CharSequence path) {
        CharBuffer cb = CharBuffer.wrap(path);
        LinkedList<CharSequence> ret = new LinkedList<>();

        int len = cb.length();
        int start = 0;
        for (int i=0; i < len; i++) {
            if (cb.charAt(i) != '/') continue;

            int a = start;
            start = i + 1;

            if (i != a) {
                ret.add(cb.subSequence(a, i));
            }
        }

        if (start != len) {
            ret.add(cb.subSequence(start, len));
        }

        return ret;
    }

    public @NotNull @Unmodifiable List<String> keys() {
        return this.root.keys();
    }

    public @Nullable Resolution<T> resolve(@NotNull CharSequence path) {
        LinkedList<CharSequence> params = new LinkedList<>();

        T out = this.root.get(this.split(path), params);
        if (out == null) return null;

        List<String> params2;
        if (params.isEmpty()) {
            params2 = Collections.emptyList();
        } else {
            params2 = new ArrayList<>(params.size());
            for (CharSequence cs : params) params2.add(cs.toString());
            params2 = Collections.unmodifiableList(params2);
        }
        return new Resolution<>(out, params2);
    }

    public @Nullable T get(@NotNull CharSequence path) {
        return this.root.get(this.split(path), null);
    }

    public void put(@NotNull CharSequence path, @NotNull T value) {
        this.root.put(this.split(path), value);
    }

    //

    public record Resolution<V>(
            @NotNull V value,
            @NotNull List<String> params
    ) { }

}
