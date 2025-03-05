package io.github.wasabithumb.xpdy.nd;

import io.github.wasabithumb.xpdy.misc.path.PathUtil;
import io.github.wasabithumb.xpdy.payload.body.Body;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

@ApiStatus.Internal
record FilesystemStaticContent(
        @NotNull Path root
) implements StaticContent {

    @Override
    public @Nullable Body serve(@NotNull String path) throws IOException {
        Path head = this.root;
        for (CharSequence part : PathUtil.split(path)) {
            head = head.resolve(part.toString());
        }

        if (!Files.exists(head))
            return null;

        if (!Files.isDirectory(head))
            return Body.file(head);

        Path index = null;

        if (Files.isDirectory(head)) {
            try (Stream<Path> stream = Files.list(head)) {
                Iterator<Path> iter = stream.iterator();
                Path next;
                while (iter.hasNext()) {
                    next = iter.next();
                    if (Files.isDirectory(next)) continue;

                    Path n = next.getFileName();
                    if (n == null) continue;

                    String name = n.toString();
                    int idx = name.indexOf('.');
                    if (idx == -1) {
                        if (!name.equals("index")) continue;
                    } else if (idx == 5) {
                        if (!name.startsWith("index")) continue;
                        if (name.length() == 10 && name.endsWith("html"))
                            return Body.file(next);
                    } else {
                        continue;
                    }

                    index = next;
                }
            }
        }

        if (index == null) return null;
        return Body.file(index);
    }

}
