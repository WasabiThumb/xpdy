package io.github.wasabithumb.xpdy.payload.body;

import io.github.wasabithumb.xpdy.misc.io.IOSupplier;
import io.github.wasabithumb.xpdy.misc.MimeType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.io.InputStream;

@ApiStatus.Internal
final class InputStreamBody extends StreamBody {

    private final IOSupplier<InputStream> streamSource;
    public InputStreamBody(
            @Range(from = -1, to = Long.MAX_VALUE) long size,
            @NotNull @MimeType String type,
            @NotNull IOSupplier<InputStream> streamSource
    ) {
        super(size, type);
        this.streamSource = streamSource;
    }

    //

    @Override
    public @NotNull InputStream stream() throws IOException {
        return this.streamSource.execute();
    }

}
