package io.github.wasabithumb.xpdy.payload.response;

import io.github.wasabithumb.xpdy.payload.Payload;
import io.github.wasabithumb.xpdy.payload.body.Body;
import io.github.wasabithumb.xpdy.payload.cookie.Cookie;
import io.github.wasabithumb.xpdy.payload.cookie.CookieName;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@ApiStatus.NonExtendable
public interface Response extends Payload {

    @Contract("_ -> new")
    static @NotNull Response of(@NotNull Body body) {
        return builder()
                .code(body.size() == 0L ? 204 : 200)
                .body(body)
                .build();
    }

    @Contract("-> new")
    static @NotNull Builder builder() {
        return new ResponseImpl.Builder();
    }

    @Contract("_ -> new")
    static @NotNull Response error(@Range(from=400, to=599) int code) {
        return builder()
                .code(code)
                .body(Body.error(code))
                .build();
    }

    //

    /**
     * The HTTP <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">response status code</a>.
     * A typical successful response will have status {@code 200} (OK).
     */
    @Contract(pure = true)
    @Range(from=100, to=599) int code();

    //

    interface Builder {

        @Contract("_ -> this")
        @NotNull Builder code(@Range(from=100, to=599) int code);

        @Contract("_ -> this")
        @NotNull Builder body(@NotNull Body body);

        @Contract("_, _ -> this")
        @NotNull Builder addHeader(@NotNull String key, @NotNull String value);

        @Contract("_, _ -> this")
        @NotNull Builder setHeader(@NotNull String key, @NotNull String value);

        @Contract("_ -> this")
        @NotNull Builder setCookie(@NotNull Cookie cookie);

        @Contract("_, _ -> this")
        default @NotNull Builder setCookie(@NotNull @CookieName String key, @NotNull String value) {
            return this.setCookie(Cookie.of(key, value));
        }

        //

        @Contract("-> new")
        @NotNull Response build();

    }

}
