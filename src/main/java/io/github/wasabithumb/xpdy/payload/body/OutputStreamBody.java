package io.github.wasabithumb.xpdy.payload.body;

import io.github.wasabithumb.xpdy.misc.io.IOConsumer;
import io.github.wasabithumb.xpdy.misc.MimeType;
import io.github.wasabithumb.xpdy.misc.io.PipeInputStream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@ApiStatus.Internal
final class OutputStreamBody extends StreamBody {

    private final IOConsumer<OutputStream> streamSource;
    public OutputStreamBody(
            @Range(from = -1, to = Long.MAX_VALUE) long size,
            @NotNull @MimeType String type,
            @NotNull IOConsumer<OutputStream> streamSource
    ) {
        super(size, type);
        this.streamSource = streamSource;
    }

    //


    @Override
    public @NotNull InputStream stream() {
        return new PipeInputStream(this::pipe);
    }

    @Override
    public void pipe(@NotNull OutputStream os) throws IOException {
        this.streamSource.execute(os);
    }

}
