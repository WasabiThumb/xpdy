package io.github.wasabithumb.xpdy.payload.body;

import com.sun.net.httpserver.HttpExchange;
import io.github.wasabithumb.xpdy.except.BodyFormatException;
import io.github.wasabithumb.xpdy.misc.io.IOConsumer;
import io.github.wasabithumb.xpdy.misc.io.IOSupplier;
import io.github.wasabithumb.xpdy.misc.MimeType;
import io.github.wasabithumb.xpdy.misc.MimeTypes;
import io.github.wasabithumb.xpdy.misc.io.URLEncodedReader;
import io.github.wasabithumb.xpdy.misc.io.URLEncodedWriter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

@ApiStatus.NonExtendable
public interface Body {

    @Contract("-> new")
    static @NotNull Builder builder() {
        return new Builder();
    }

    @Contract("_ -> new")
    static @NotNull Body exchange(@NotNull HttpExchange exchange) {
        if (exchange.getRequestMethod().equals("GET"))
            return new EmptyBody(MimeTypes.TEXT);
        return new ExchangeBody(exchange);
    }

    @Contract("_ -> new")
    static @NotNull Body file(@NotNull Path file) throws IOException {
        Builder b = builder()
                .source(() -> Files.newInputStream(file, StandardOpenOption.READ))
                .size(Files.size(file));

        //noinspection PatternValidation
        String type = Files.probeContentType(file);
        if (type != null) {
            //noinspection PatternValidation
            b.type(type);
        }

        return b.build();
    }

    @Contract("_ -> new")
    static @NotNull Body text(@NotNull String text) {
        return builder()
                .source(text.getBytes(StandardCharsets.UTF_8))
                .type(MimeTypes.TEXT)
                .build();
    }

    @Contract("_ -> new")
    static @NotNull Body html(@NotNull String html) {
        return builder()
                .source(html.getBytes(StandardCharsets.UTF_8))
                .type(MimeTypes.HTML)
                .build();
    }

    @Contract("_ -> new")
    static @NotNull Body urlencoded(@NotNull Map<String, String> map) {
        return builder()
                .source(URLEncodedWriter.serializeUTF8(map))
                .type(MimeTypes.URLENCODED)
                .build();
    }

    @Contract("_ -> new")
    static @NotNull Body error(@Range(from=400, to=599) int code) {
        final String rel = (code < 500) ? "Client" : "Server";
        return html("<!DOCTYPE html><html lang=\"en\"><head><title>" + code + "</title><meta charset=\"UTF-8\">" +
                "<style>body,footer,html{position:absolute}a:hover,footer{opacity:.6}body,html{width:100vw;" +
                "height:100vh;height:100dvh;margin:0;padding:0}body{background:#eee;color:#000;flex-direction:column;" +
                "display:flex;justify-content:center;align-items:center;font-family:\"JetBrains Mono\",monospace}" +
                "@media (prefers-color-scheme:dark){body{background:#111;color:#eee}}a{color:inherit;cursor:pointer;" +
                "text-decoration:none;opacity:1;transition:opacity .2s ease-in-out}h1{display:inline;" +
                "font-size:min(24vw,24vh)}footer{left:0;bottom:0;width:100%;text-align:center;font-size:min(3vw,3vh);" +
                "padding-bottom:.5em}</style></head><body>" +
                "<a href=\"https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/" + code + "\" target=\"_blank\">" +
                "<h1>" + code + "</h1></a><footer><span>" + rel + " Error</span>&nbsp;&bull;&nbsp;" +
                "<a href=\"https://github.com/WasabiThumb/xpdy\">xpdy</a></footer></body></html>");
    }

    //

    /**
     * Size in bytes of the body. If {@code -1}, size is unknown.
     */
    @Range(from=-1, to=Long.MAX_VALUE) long size();

    /**
     * The MIME type of the body.
     */
    @NotNull @MimeType String type();

    /**
     * Reads the content of the body.
     */
    @NotNull InputStream stream() throws IOException;

    /**
     * Writes the content of the body.
     */
    default void pipe(@NotNull OutputStream os) throws IOException {
        try (InputStream is = this.stream()) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) {
                os.write(buf, 0, r);
            }
            os.flush();
        }
    }

    /**
     * Reads the content of the body into a byte array.
     */
    default byte @NotNull [] bytes() throws IOException {
        long s = this.size();
        if (s == 0L) return new byte[0];
        try (InputStream is = this.stream()) {
            if (s == -1L || s > Integer.MAX_VALUE) return is.readAllBytes();
            return is.readNBytes((int) s);
        }
    }

    /**
     * Reads the content of the body into a string.
     */
    default @NotNull String text() throws IOException, BodyFormatException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = this.stream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)
        ) {
            char[] buf = new char[4096];
            int r;
            while ((r = isr.read(buf)) != -1)
                sb.append(buf, 0, r);
        } catch (CharacterCodingException cce) {
            throw new BodyFormatException("Body cannot be read as UTF-8", cce);
        }
        return sb.toString();
    }

    /**
     * Reads the content of the body as urlencoded.
     */
    default @NotNull Map<String, String> urlencoded() throws IOException, BodyFormatException {
        try (InputStream is = this.stream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             URLEncodedReader uer = new URLEncodedReader(isr)
        ) {
            return uer.readURLEncoded();
        }
    }

    /**
     * Converts this body into a body of the specified type.
     * If the primary constructor of the body class throws an unchecked exception, it is rethrown as-is.
     */
    default <I extends Body> @NotNull I as(@NotNull Class<I> clazz) {
        if (clazz.isInstance(this)) return clazz.cast(this);
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))
            throw new IllegalArgumentException("Cannot wrap into abstract body type " + clazz.getName());

        Constructor<I> con;
        try {
            con = clazz.getDeclaredConstructor(Body.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Body class " + clazz.getName() + " has no wrapper constructor");
        }

        con.trySetAccessible();

        try {
            return con.newInstance(this);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) throw re;
            throw new IllegalStateException(
                    "Wrapper constructor for body class " + clazz.getName() +
                    " raised a checked exception",
                    (cause == null) ? e : cause
            );
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new AssertionError(
                    "Failed to access wrapper constructor for body class " + clazz.getName(),
                    e
            );
        }
    }

    //

    final class Builder {

        private SourceType sourceType = SourceType.EMPTY;
        private Object source = null;
        private long size = -1L;
        private String type = MimeTypes.BYTES;

        //

        @Contract("_ -> this")
        public @NotNull Builder source(@NotNull HttpExchange exchange) {
            this.sourceType = SourceType.EXCHANGE;
            this.source = exchange;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder source(@NotNull IOSupplier<InputStream> source) {
            this.sourceType = SourceType.INPUT_STREAM;
            this.source = source;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder source(@NotNull InputStream stream) {
            return this.source(() -> stream);
        }

        @Contract("_ -> this")
        public @NotNull Builder source(@NotNull IOConsumer<OutputStream> source) {
            this.sourceType = SourceType.OUTPUT_STREAM;
            this.source = source;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder source(byte @NotNull [] bytes) {
            this.size = bytes.length;
            return this.source((OutputStream os) -> os.write(bytes));
        }

        @Contract("_ -> this")
        public @NotNull Builder size(@Range(from=-1, to=Long.MAX_VALUE) long size) {
            this.size = size;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder type(@NotNull @MimeType String type) {
            this.type = type;
            return this;
        }

        @SuppressWarnings("unchecked")
        @Contract("-> new")
        public @NotNull Body build() {
            return switch (this.sourceType) {
                case EMPTY -> new EmptyBody(this.type);
                case INPUT_STREAM -> new InputStreamBody(this.size, this.type, (IOSupplier<InputStream>) this.source);
                case OUTPUT_STREAM -> new OutputStreamBody(this.size, this.type, (IOConsumer<OutputStream>) this.source);
                case EXCHANGE -> new ExchangeBody((HttpExchange) this.source);
            };
        }

        //

        private enum SourceType {
            EMPTY,
            INPUT_STREAM,
            OUTPUT_STREAM,
            EXCHANGE
        }

    }

}
