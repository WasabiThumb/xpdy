package io.github.wasabithumb.xpdy.payload.body;

import io.github.wasabithumb.xpdy.except.BodyFormatException;
import io.github.wasabithumb.xpdy.misc.MimeType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@ApiStatus.Internal
public abstract class FilterBody implements Body {

    protected final Body backing;
    public FilterBody(@NotNull Body backing) {
        this.backing = backing;
    }

    //

    @Override
    public @Range(from = -1, to = Long.MAX_VALUE) long size() {
        return this.backing.size();
    }

    @Override
    public @NotNull @MimeType String type() {
        return this.backing.type();
    }

    @Override
    public @NotNull InputStream stream() throws IOException {
        return this.backing.stream();
    }

    @Override
    public void pipe(@NotNull OutputStream os) throws IOException {
        this.backing.pipe(os);
    }

    @Override
    public byte @NotNull [] bytes() throws IOException {
        return this.backing.bytes();
    }

    @Override
    public @NotNull String text() throws IOException {
        return this.backing.text();
    }

    @Override
    public @NotNull Map<String, String> urlencoded() throws IOException, BodyFormatException {
        return this.backing.urlencoded();
    }

}
