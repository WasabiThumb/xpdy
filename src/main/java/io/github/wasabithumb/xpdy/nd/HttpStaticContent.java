package io.github.wasabithumb.xpdy.nd;

import io.github.wasabithumb.xpdy.XpdyServer;
import io.github.wasabithumb.xpdy.misc.MimeTypes;
import io.github.wasabithumb.xpdy.misc.MimeUtil;
import io.github.wasabithumb.xpdy.misc.io.IOSupplier;
import io.github.wasabithumb.xpdy.misc.path.PathUtil;
import io.github.wasabithumb.xpdy.payload.body.Body;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@ApiStatus.Internal
record HttpStaticContent(
        @NotNull URL root
) implements StaticContent {

    HttpStaticContent {
        String protocol = root.getProtocol();
        int len = protocol.length();
        boolean matches = (len == 5) ?
                protocol.equalsIgnoreCase("https") :
                (len == 4 && protocol.equalsIgnoreCase("http"));
        if (!matches)
            throw new IllegalArgumentException("\"" + root + "\" is not an HTTP(S) URL");
    }

    //

    private @NotNull URL resolve(@NotNull String path) throws IOException {
        try {
            URI uri = this.root.toURI();
            for (CharSequence part : PathUtil.split(path)) {
                uri = uri.resolve(part.toString());
            }
            return uri.toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Unable to concatenate URL \"" + this.root + "\" with path \"" + path + "\"", e);
        }
    }

    private @NotNull HttpURLConnection open(@NotNull String path) throws IOException {
        return (HttpURLConnection) this.resolve(path).openConnection();
    }

    //

    @Override
    @SuppressWarnings("PatternValidation")
    public @Nullable Body serve(@NotNull String path) throws IOException {
        HttpURLConnection c = this.open(path);
        c.setRequestProperty("Accept", "*/*");
        c.setRequestProperty("User-Agent", "xpdy/" + XpdyServer.version());

        int code = c.getResponseCode();
        if (code == 404) return null;
        if (code == 204) {
            return Body.builder()
                    .size(0L)
                    .type(MimeTypes.HTML)
                    .build();
        }

        IOSupplier<InputStream> source;
        long size = c.getHeaderFieldLong("Content-Length", -1L);
        String type = c.getHeaderField("Content-Type");

        if (type == null) {
            final int capacity = 32;
            final byte[] buf = new byte[capacity];
            int len = 0;
            int read;
            boolean close = true;

            InputStream is = c.getInputStream();
            try {
                while (len < capacity) {
                    read = is.read(buf, len, capacity - len);
                    if (read == -1) break;
                    len += read;
                }

                try (ByteArrayInputStream bis = new ByteArrayInputStream(buf)) {
                    type = MimeUtil.detect(bis);
                }
                if (type == null) type = MimeTypes.HTML;

                if (len < capacity) {
                    final int fl = len;
                    source = () -> new ByteArrayInputStream(buf, 0, fl);
                } else {
                    close = false;
                    source = () -> new SequenceInputStream(new ByteArrayInputStream(buf), is);
                }
            } finally {
                if (close) is.close();
            }
        } else {
            source = c::getInputStream;
        }

        return Body.builder()
                .source(source)
                .size(size)
                .type(type)
                .build();
    }

}
