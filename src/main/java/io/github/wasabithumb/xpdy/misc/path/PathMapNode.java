package io.github.wasabithumb.xpdy.misc.path;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

@ApiStatus.Internal
final class PathMapNode<T> {

    private static int cmp(@NotNull CharSequence a, @NotNull CharSequence b) {
        int al = a.length();
        int bl = b.length();

        boolean am = al < bl;
        int m = am ? al : bl;
        int c;

        for (int i=0; i < m; i++) {
            c = Character.compare(
                    Character.toLowerCase(a.charAt(i)),
                    Character.toLowerCase(b.charAt(i))
            );
            if (c != 0) return c;
        }

        if (am) {
            return -1;
        } else if (al == bl) {
            return 0;
        } else {
            return 1;
        }
    }

    //

    private T identity;
    private int capacity;
    private int length;
    private Object[] children;

    PathMapNode(
            int capacity
    ) {
        this.identity = null;
        this.capacity = capacity;
        this.length = 0;
        this.children = new Object[capacity];
    }

    PathMapNode() {
        this(8);
    }

    //

    @SuppressWarnings("unchecked")
    private @NotNull Entry<T> child(int index) {
        return (Entry<T>) this.children[index];
    }

    public int size() {
        int ret = (this.identity == null) ? 0 : 1;
        for (int i=0; i < this.length; i++) {
            ret += this.child(i).node.size();
        }
        return ret;
    }

    public @Nullable T get(@NotNull Queue<CharSequence> path, @Nullable Queue<CharSequence> params) {
        CharSequence next = path.poll();
        if (next == null) return this.identity;

        Entry<T> child;
        PathMapNode<T> wildcard = null;
        for (int i=0; i < this.length; i++) {
            child = this.child(i);
            if (child.label.equals("*")) {
                wildcard = child.node;
            } else if (cmp(next, child.label) == 0) {
                return child.node.get(path, params);
            }
        }

        if (wildcard != null) {
            if (params != null) params.add(next);
            return wildcard.get(path, params);
        }

        return null;
    }

    public void put(@NotNull Queue<CharSequence> path, @NotNull T value) {
        CharSequence next = path.poll();
        if (next == null) {
            this.identity = value;
            return;
        }

        Entry<T> child;
        for (int i=0; i < this.length; i++) {
            child = this.child(i);
            if (cmp(next, child.label) == 0) {
                child.node.put(path, value);
                return;
            }
        }

        child = new Entry<>(
                next.toString(),
                new PathMapNode<>()
        );
        this.insert(child);
        child.node.put(path, value);
    }

    private void insert(@NotNull Entry<T> entry) {
        if (this.length == this.capacity) {
            int nc = this.capacity * 2;
            Object[] cpy = new Object[nc];
            System.arraycopy(this.children, 0, cpy, 0, this.length);
            this.children = cpy;
            this.capacity = nc;
        }
        for (int i=0; i < this.length; i++) {
            if (cmp(this.child(i).label, entry.label) > 0) {
                System.arraycopy(this.children, i, this.children, i + 1, this.length - i);
                this.children[i] = entry;
                this.length++;
                return;
            }
        }
        this.children[this.length++] = entry;
    }

    public @NotNull @Unmodifiable List<String> keys() {
        List<String> ret = new ArrayList<>(this.size());
        this.keys0(new StringBuilder("/"), ret);
        return Collections.unmodifiableList(ret);
    }

    private void keys0(@NotNull StringBuilder sb, @NotNull List<String> out) {
        if (this.identity != null) out.add(sb.toString());
        int prefixLen = sb.length();

        Entry<T> child;
        for (int i=0; i < this.length; i++) {
            child = this.child(i);
            sb.setLength(prefixLen);
            sb.append(child.label).append('/');
            child.node.keys0(sb, out);
        }
    }

    //

    private record Entry<V>(
            @NotNull String label,
            @NotNull PathMapNode<V> node
    ) { }

}
