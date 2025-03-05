package io.github.wasabithumb.xpdy.misc.path;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static io.github.wasabithumb.xpdy.misc.path.PathUtil.split;

/**
 * An intrinsically sorted map where the keys are URI paths.
 * If a path part is an asterisk ({@code *}), it acts as a wildcard.
 */
@ApiStatus.Internal
public final class PathMap<T> {

    private final PathMapNode<T> root;

    private PathMap(@NotNull PathMapNode<T> root) {
        this.root = root;
    }

    @ApiStatus.AvailableSince("0.2.0")
    public PathMap(boolean allowWildcard) {
        this(new PathMapNode<>(allowWildcard));
    }

    public PathMap() {
        this(true);
    }

    //

    public @NotNull @Unmodifiable List<String> keys() {
        return this.root.keys();
    }

    @ApiStatus.AvailableSince("0.2.0")
    public @NotNull @Unmodifiable List<T> values(boolean deep) {
        return this.root.values(deep);
    }

    @ApiStatus.AvailableSince("0.2.0")
    public @NotNull PathMap<T> sub(@NotNull CharSequence path) throws IllegalArgumentException {
        PathMapNode<T> head = this.root;
        for (CharSequence part : split(path)) {
            head = head.sub(part);
            if (head == null) {
                throw new IllegalArgumentException("Path \"" + path + "\" does not exist in map");
            }
        }
        return new PathMap<>(head);
    }

    public @Nullable Resolution<T> resolve(@NotNull CharSequence path) {
        LinkedList<CharSequence> params = new LinkedList<>();

        T out = this.root.get(split(path), params);
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
        return this.root.get(split(path), null);
    }

    public void put(@NotNull CharSequence path, @NotNull T value) {
        this.root.put(split(path), value);
    }

    //

    public record Resolution<V>(
            @NotNull V value,
            @NotNull List<String> params
    ) { }

}
