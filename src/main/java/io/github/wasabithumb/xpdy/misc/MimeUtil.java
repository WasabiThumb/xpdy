package io.github.wasabithumb.xpdy.misc;

import io.github.wasabithumb.xpdy.misc.io.IOSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Combines utilities for inferring MIME types.
 * Hooks into Apache Tika if present on classpath.
 * @since 0.2.0
 */
@ApiStatus.Internal
@ApiStatus.AvailableSince("0.2.0")
@SuppressWarnings("PatternValidation")
public final class MimeUtil {

    private static final boolean USE_TIKA;
    private static final Object TIKA;
    private static final Method TIKA_DETECT_PATH;
    private static final Method TIKA_DETECT_STREAM;
    static {
        boolean useTika = false;
        Object tika = null;
        Method tikaDetectPath = null;
        Method tikaDetectStream = null;
        try {
            Class<?> cTika = Class.forName("org.apache.tika.Tika");
            tika = cTika.getConstructor().newInstance();
            tikaDetectPath = cTika.getMethod("detect", Path.class);
            tikaDetectStream = cTika.getMethod("detect", InputStream.class);
            useTika = true;
        } catch (ReflectiveOperationException | SecurityException ignored) {
        } finally {
            USE_TIKA = useTika;
            TIKA = tika;
            TIKA_DETECT_PATH = tikaDetectPath;
            TIKA_DETECT_STREAM = tikaDetectStream;
        }
    }

    //

    public static @Nullable @MimeType String detect(@NotNull Path path) throws IOException {
        if (USE_TIKA) {
            Object out;
            try {
                out = TIKA_DETECT_PATH.invoke(TIKA, path);
            } catch (ReflectiveOperationException e) {
                rethrow(e);
                return null;
            }
            if (out != null) {
                return (String) out;
            }
        }
        return Files.probeContentType(path);
    }

    public static @Nullable @MimeType String detect(@NotNull InputStream stream) throws IOException {
        if (USE_TIKA) {
            Object out;
            try {
                out = TIKA_DETECT_STREAM.invoke(TIKA, stream);
            } catch (ReflectiveOperationException e) {
                rethrow(e);
                return null;
            }
            if (out != null) {
                return (String) out;
            }
        }
        return URLConnection.guessContentTypeFromStream(new BufferedInputStream(stream));
    }

    public static @NotNull @MimeType String detectElse(
            @NotNull IOSupplier<InputStream> stream,
            @NotNull @MimeType String fallback
    ) {
        try (InputStream is = stream.execute()) {
            String detected = detect(is);
            if (detected != null) return detected;
        } catch (IOException ignored) { }
        return fallback;
    }

    //

    @Contract("_ -> fail")
    private static void rethrow(@NotNull ReflectiveOperationException e) throws IOException {
        if (e instanceof InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof IOException io) throw io;
            if (cause instanceof RuntimeException re) throw re;
            throw new IllegalStateException("Tika raised a checked exception", e);
        }
        throw new AssertionError("Unexpected reflection error", e);
    }

}
