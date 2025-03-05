package io.github.wasabithumb.xpdy.nd;

import io.github.wasabithumb.xpdy.XpdyServer;
import io.github.wasabithumb.xpdy.except.ServeException;
import io.github.wasabithumb.xpdy.misc.URIPath;
import io.github.wasabithumb.xpdy.misc.path.PathUtil;
import io.github.wasabithumb.xpdy.payload.body.Body;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serves static content
 * @see #serve(String)
 * @since 0.2.0
 */
@ApiStatus.AvailableSince("0.2.0")
@FunctionalInterface
public interface StaticContent {

    /**
     * Provides a {@link StaticContent} instance which always serves {@code null}; default for a server
     * if not otherwise set
     * @since 0.2.0
     */
    static @NotNull StaticContent empty() {
        return EmptyStaticContent.INSTANCE;
    }

    /**
     * Creates a new {@link StaticContent} instance which attempts to serve paths by deferring to each
     * {@link StaticContent} provided here in turn until one returns non-null.
     * @since 0.2.0
     */
    @Contract("_, _ -> new")
    static @NotNull StaticContent of(@NotNull StaticContent a, @NotNull StaticContent @NotNull ... b) {
        return new PolyStaticContent(a, b);
    }

    /**
     * Creates a new {@link StaticContent} instance which serves paths by deferring to the filesystem,
     * where the path {@code /} corresponds to the directory named by the {@code root} parameter.
     * @since 0.2.0
     */
    @Contract("_ -> new")
    static @NotNull StaticContent filesystem(@NotNull Path root) {
        return new FilesystemStaticContent(root);
    }

    /**
     * Creates a new {@link StaticContent} instance which serves paths by reading from a ZIP/JAR file
     * named by the {@code archive} parameter.
     * @param prefix The directory within the archive that should map to the path {@code /}
     * @since 0.2.0
     */
    @Contract("_, _ -> new")
    static @NotNull StaticContent archive(@NotNull Path archive, @NotNull String prefix) {
        return new ZipStaticContent(archive, prefix);
    }

    /**
     * Creates a new {@link StaticContent} instance which serves paths by reading from a ZIP/JAR file
     * named by the {@code archive} parameter.
     * @since 0.2.0
     * @see #archive(Path, String)
     */
    @Contract("_ -> new")
    static @NotNull StaticContent archive(@NotNull Path archive) {
        return new ZipStaticContent(archive, "");
    }

    /**
     * Creates a new {@link StaticContent} instance which serves paths by redirecting requests to the
     * specified HTTP(S) URL.
     * @since 0.2.0
     */
    @Contract("_ -> new")
    static @NotNull StaticContent http(@NotNull URL url) {
        return new HttpStaticContent(url);
    }

    /**
     * Creates a new {@link StaticContent} instance which serves the {@link Class#getResource(String) resources}
     * of the calling class. Resources are resolved on a best-effort basis (the same name may exist in multiple sibling
     * class loaders); especially when the {@code root} is empty.
     * @param root The resource path which maps to the server root path ({@code /}).
     * @since 0.2.0
     * @see #archive(Path, String)
     * @see #filesystem(Path)
     */
    static @NotNull StaticContent resources(@NotNull String root) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = ClassLoader.getSystemClassLoader();

        StringBuilder sb = new StringBuilder();
        for (CharSequence part : PathUtil.split(root)) {
            sb.append(part).append('/');
        }

        int sbl = sb.length();
        if (sbl == 0) {
            // Special case
            Class<?> cls = XpdyServer.class;
            String packageName = cls.getPackageName();
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            String cn;
            for (int i=1; i < trace.length; i++) {
                cn = trace[i].getClassName();
                if (!cn.startsWith(packageName)) {
                    try {
                        cls = Class.forName(cn, false, cl);
                        break;
                    } catch (ClassNotFoundException ignored) { }
                }
            }
            Path p;
            try {
                p = Path.of(cls.getProtectionDomain().getCodeSource().getLocation().toURI());
            } catch (URISyntaxException | IllegalArgumentException | SecurityException | NullPointerException e) {
                throw new IllegalStateException("Failed to idenitfy code source of class " + cls.getName(), e);
            }
            if (Files.isDirectory(p)) {
                return filesystem(p);
            } else {
                return archive(p);
            }
        }
        sb.setLength(--sbl);
        root = sb.toString();

        URL url = cl.getResource(root);
        if (url == null) throw new IllegalArgumentException("Cannot find \"" + root + "\" in class loader " + cl);

        String protocol = url.getProtocol();
        if (protocol.equals("file")) {
            Path path = Path.of(url.getPath());
            return filesystem(path);
        } else if (protocol.equals("jar")) {
            String p = url.getPath();
            if (!p.startsWith("file:")) throw new IllegalStateException("Malformed JAR URL " + url);

            int i = p.indexOf('!');
            if (i == -1) {
                p = p.substring(5);
            } else {
                p = p.substring(5, i);
            }

            return archive(Path.of(p), root);
        } else {
            throw new IllegalStateException("Unable to handle protocol of resource URL " + url);
        }
    }

    //

    /**
     * Attempts to serve the static content at the given path. Paths will have a leading slash and may have a
     * trailing slash.
     * @return The body, or null if no content exists.
     * @throws IOException I/O error while serving the content
     * @throws ServeException Any issue while serving the content
     */
    @ApiStatus.OverrideOnly
    @Nullable Body serve(@NotNull @URIPath String path) throws IOException;

}
