package io.github.wasabithumb.xpdy.nd;

import io.github.wasabithumb.xpdy.misc.MimeTypes;
import io.github.wasabithumb.xpdy.misc.io.IOSupplier;
import io.github.wasabithumb.xpdy.misc.path.PathMap;
import io.github.wasabithumb.xpdy.payload.body.Body;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

final class ZipStaticContent implements StaticContent {

    private static final byte[] ZIP_HEADER = new byte[] { 0x50, 0x4B, 0x03, 0x04 };
    private static void checkReadableZip(@NotNull Path path) {
        Throwable suppressed = null;
        try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
            if (Arrays.equals(ZIP_HEADER, is.readNBytes(4))) return;
        } catch (IOException e) {
            suppressed = e;
        }
        IllegalArgumentException iae =
                new IllegalArgumentException(path.toAbsolutePath() + " is not a path to a readable ZIP/JAR file");
        if (suppressed != null) iae.addSuppressed(suppressed);
        throw iae;
    }

    //

    private final Path zip;
    private final String prefix;
    private final Listing listing;

    ZipStaticContent(@NotNull Path zip, @NotNull String prefix) {
        checkReadableZip(zip);
        this.zip = zip;
        this.prefix = prefix;
        this.listing = new Listing(zip);
    }

    @Override
    public @Nullable Body serve(@NotNull String path) throws IOException {
        ZipEntry ze = this.listing.get(this.prefix + path);
        if (ze != null && !ze.isDirectory()) return this.createBody(ze);

        ZipEntry index = null;
        for (ZipEntry child : this.listing.list(this.prefix + path)) {
            String name = child.getName();
            int len = name.length();
            if (len == 0 || name.charAt(len - 1) == '/') continue;

            int ws = name.lastIndexOf('/') + 1;
            name = name.substring(ws);

            int wd = name.indexOf('.');
            if (wd == -1) {
                if (!name.equals("index")) continue;
            } else if (wd == 5) {
                if (!name.startsWith("index")) continue;
                if ((len - ws) == 10 && name.endsWith("html"))
                    return this.createBody(child);
            } else {
                continue;
            }
            index = child;
        }
        if (index == null) return null;
        return this.createBody(index);
    }

    @SuppressWarnings("PatternValidation")
    private @NotNull Body createBody(@NotNull ZipEntry ze) {
        String name = ze.getName();
        int len = name.length();
        if (len == 0 || name.charAt(len - 1) == '/')
            throw new IllegalArgumentException("Entry is invalid or directory");

        IOSupplier<InputStream> reader = this.createReader(name);

        int sep = name.lastIndexOf('/');
        if (sep != -1) name = name.substring(sep + 1);
        String type = MimeTypes.infer(name, reader, MimeTypes.BYTES);

        return Body.builder()
                .source(reader)
                .size(ze.getSize())
                .type(type)
                .build();
    }

    private @NotNull IOSupplier<InputStream> createReader(final @NotNull String name) {
        return () -> {
            ZipFile zf = null;
            boolean close = true;
            try {
                zf = new ZipFile(this.zip.toFile());
                ZipEntry ze = zf.getEntry(name);
                if (ze == null) {
                    throw new IOException("Entry \"" + name + "\" is no longer present in archive @ " +
                            this.zip.toAbsolutePath());
                }
                InputStream is = zf.getInputStream(ze);
                close = false;
                return closeListener(is, zf);
            } finally {
                if (close && zf != null) zf.close();
            }
        };
    }

    private static @NotNull InputStream closeListener(@NotNull InputStream stream, @NotNull Closeable attached) {
        return new FilterInputStream(stream) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    attached.close();
                }
            }
        };
    }

    //

    private static final class Listing {

        private final Path zip;
        private final PathMap<ZipEntry> map = new PathMap<>(false);
        private boolean ready = false;

        Listing(@NotNull Path zip) {
            this.zip = zip;
        }

        private synchronized void fill() throws IOException {
            if (this.ready) return;
            try (InputStream fis = Files.newInputStream(this.zip, StandardOpenOption.READ);
                 ZipInputStream zis = new ZipInputStream(fis)
            ) {
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    this.map.put(ze.getName(), ze);
                }
            }
            this.ready = true;
        }

        public @Nullable ZipEntry get(@NotNull String path) throws IOException {
            this.fill();
            return this.map.get(path);
        }

        public @NotNull @Unmodifiable List<ZipEntry> list(@NotNull String parent) throws IOException {
            this.fill();
            try {
                return this.map.sub(parent).values(false);
            } catch (IllegalArgumentException e) {
                return Collections.emptyList();
            }
        }

    }

}
