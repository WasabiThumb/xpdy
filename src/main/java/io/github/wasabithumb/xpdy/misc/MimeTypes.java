package io.github.wasabithumb.xpdy.misc;

import io.github.wasabithumb.xpdy.misc.io.IOSupplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * A bank of common MIME types.
 * @see #infer(String, IOSupplier, String)
 */
public final class MimeTypes {

    /**
     * Infers the MIME type of a resource using the first successful method in the following list:
     * <ul>
     *     <li>Checking {@code name} against known extensions</li>
     *     <li>Opening {@code source} and looking for magic bytes</li>
     *     <li>Returning {@code fallback}</li>
     * </ul>
     * @since 0.2.1
     */
    @SuppressWarnings("PatternValidation")
    @Contract("_, _, !null -> !null")
    public static @Nullable @MimeType String infer(
            @NotNull String name,
            @NotNull IOSupplier<InputStream> source,
            @Nullable @MimeType String fallback
    ) {
        String ret = MimeUtil.detect(name);
        if (ret != null) return ret;

        try (InputStream is = source.execute()) {
            ret = MimeUtil.detect(is);
        } catch (IOException ignored) { }
        if (ret != null) return ret;

        return fallback;
    }

    //

    @MimeType
    public static final String BYTES = "application/octet-stream";

    @MimeType
    public static final String TEXT = "text/plain";

    @MimeType
    public static final String HTML = "text/html";

    @MimeType
    public static final String URLENCODED = "application/x-www-form-urlencoded";

    @MimeType
    public static final String JSON = "application/json";

}
