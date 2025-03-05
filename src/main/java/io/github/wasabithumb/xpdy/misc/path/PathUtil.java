package io.github.wasabithumb.xpdy.misc.path;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.Queue;

@ApiStatus.Internal
public final class PathUtil {

    public static @NotNull Queue<CharSequence> split(@NotNull CharSequence path) {
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

}
