package io.github.wasabithumb.xpdy.payload.cookie;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
record ShallowCookie(
        @NotNull @CookieName String name,
        @NotNull String value
) implements Cookie {

    @Override
    public boolean isShallow() {
        return true;
    }

}
