package io.github.wasabithumb.xpdy.payload.body;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.github.wasabithumb.xpdy.misc.MimeType;
import io.github.wasabithumb.xpdy.misc.MimeTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.InputStream;

@ApiStatus.Internal
record ExchangeBody(
        @NotNull HttpExchange handle
) implements Body {

    @Override
    public @Range(from = -1, to = Long.MAX_VALUE) long size() {
        Headers h = this.handle.getRequestHeaders();
        String text = h.getFirst("Content-Length");
        if (text != null) {
            try {
                long s = Long.parseLong(text);
                if (s >= 0L) return s;
            } catch (NumberFormatException ignored) { }
        }
        return -1L;
    }

    @Override
    @SuppressWarnings("PatternValidation")
    public @NotNull @MimeType String type() {
        Headers h = this.handle.getRequestHeaders();
        String text = h.getFirst("Content-Type");
        return (text == null) ? MimeTypes.BYTES : text;
    }

    @Override
    public @NotNull InputStream stream() {
        return this.handle.getRequestBody();
    }

}
