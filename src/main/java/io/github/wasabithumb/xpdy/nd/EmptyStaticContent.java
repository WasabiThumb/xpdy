package io.github.wasabithumb.xpdy.nd;

import io.github.wasabithumb.xpdy.payload.body.Body;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
final class EmptyStaticContent implements StaticContent {

    public static final EmptyStaticContent INSTANCE = new EmptyStaticContent();

    //

    @Override
    public @Nullable Body serve(@NotNull String path) {
        return null;
    }

}
