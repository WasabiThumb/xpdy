package io.github.wasabithumb.xpdy.payload.body;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import io.github.wasabithumb.xpdy.except.BodyFormatException;
import io.github.wasabithumb.xpdy.misc.MimeTypes;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * A {@link Body} extended with JSON support.
 */
public final class JsonBody extends FilterBody {

    private static final Gson DEFAULT_GSON = new Gson();

    //

    /**
     * Creates a new JSON body.
     * @param stream If true, the encoded JSON is not buffered on the server.
     *               This may be necessary for large payloads, but introduces overhead.
     */
    public static @NotNull JsonBody of(@NotNull Gson gson, @NotNull JsonElement element, boolean stream) {
        Body.Builder builder;
        if (stream) {
            builder = Body.builder().source((OutputStream os) -> {
                try (OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                     JsonWriter jw = new JsonWriter(osw)
                ) {
                    gson.toJson(element, jw);
                }
            });
        } else {
            byte[] data = gson.toJson(element).getBytes(StandardCharsets.UTF_8);
            builder = Body.builder().source(data);
        }
        builder.type(MimeTypes.JSON);
        return new JsonBody(builder.build());
    }

    /**
     * Creates a new buffered JSON body.
     * @see #of(Gson, JsonElement, boolean)
     */
    public static @NotNull JsonBody of(@NotNull Gson gson, @NotNull JsonElement element) {
        return of(gson, element, false);
    }

    /**
     * Creates a new JSON body using the default serializer.
     * @param stream If true, the encoded JSON is not buffered on the server.
     *               This may be necessary for very large payloads, but introduces overhead.
     * @see #of(Gson, JsonElement, boolean)
     */
    public static @NotNull JsonBody of(@NotNull JsonElement element, boolean stream) {
        return of(DEFAULT_GSON, element, stream);
    }

    /**
     * Creates a new buffered JSON body using the default serializer.
     * @see #of(Gson, JsonElement, boolean)
     */
    public static @NotNull JsonBody of(@NotNull JsonElement element) {
        return of(DEFAULT_GSON, element);
    }

    //

    JsonBody(@NotNull Body backing) {
        super(backing);
    }

    //

    /**
     * Parses the body as JSON.
     */
    public <T extends JsonElement> @NotNull T json(
            @NotNull Gson gson,
            @NotNull Class<T> type
    ) throws IOException, BodyFormatException {
        try (InputStream is = this.stream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        ) {
            return gson.fromJson(isr, type);
        } catch (JsonIOException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException io) throw io;
            throw new IOException(e);
        } catch (JsonSyntaxException e) {
            throw new BodyFormatException("Body is not valid JSON", e);
        }
    }

    /**
     * Parses the body as JSON using the default serializer.
     * @see #json(Gson, Class)
     */
    public <T extends JsonElement> @NotNull T json(
            @NotNull Class<T> type
    ) throws IOException, BodyFormatException {
        return this.json(DEFAULT_GSON, type);
    }

    /**
     * Parses the body as JSON.
     * @see #json(Gson, Class)
     */
    public @NotNull JsonElement json(@NotNull Gson gson) throws IOException, BodyFormatException {
        return this.json(gson, JsonElement.class);
    }

    /**
     * Parses the body as JSON using the default serializer.
     * @see #json(Gson, Class)
     */
    public @NotNull JsonElement json() throws IOException, BodyFormatException {
        return this.json(JsonElement.class);
    }

}
