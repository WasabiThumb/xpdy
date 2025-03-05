package io.github.wasabithumb.xpdy.nd;

import io.github.wasabithumb.xpdy.payload.body.Body;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

final class PolyStaticContent implements StaticContent {

    private final StaticContent[] sub;
    PolyStaticContent(@NotNull StaticContent a, @NotNull StaticContent @NotNull [] b) {
        this.sub = new StaticContent[b.length + 1];
        this.sub[0] = a;
        System.arraycopy(b, 0, this.sub, 1, b.length);
    }

    //

    @Override
    public @Nullable Body serve(@NotNull String path) throws IOException {
        Body body;
        for (StaticContent sc : this.sub) {
            body = sc.serve(path);
            if (body != null) return body;
        }
        return null;
    }

}
