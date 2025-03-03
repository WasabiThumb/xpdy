package io.github.wasabithumb.xpdy.payload.body;

import io.github.wasabithumb.xpdy.misc.MimeType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@ApiStatus.Internal
abstract class StreamBody implements Body {

    private final long size;
    private final String type;

    protected StreamBody(
            @Range(from=-1, to=Long.MAX_VALUE) long size,
            @NotNull @MimeType String type
    ) {
        this.size = size;
        this.type = type;
    }

    //

    @Override
    public @Range(from = -1, to = Long.MAX_VALUE) long size() {
        return this.size;
    }

    @Override
    public @NotNull @MimeType String type() {
        return this.type;
    }

}
