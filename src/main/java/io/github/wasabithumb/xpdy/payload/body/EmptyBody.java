package io.github.wasabithumb.xpdy.payload.body;

import io.github.wasabithumb.xpdy.misc.MimeType;
import org.jetbrains.annotations.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

@ApiStatus.Internal
record EmptyBody(
        @NotNull @MimeType String type
) implements Body {

    @Override
    public @Range(from=0, to=0) long size() {
        return 0L;
    }

    @Override
    public @NotNull InputStream stream() {
        return InputStream.nullInputStream();
    }

    @Override
    public void pipe(@NotNull OutputStream os) { }

    @Override
    public @NotNull @Unmodifiable Map<String, String> urlencoded() {
        return Collections.emptyMap();
    }

}
