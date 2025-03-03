package io.github.wasabithumb.xpdy.payload.response;

import com.sun.net.httpserver.Headers;
import io.github.wasabithumb.xpdy.payload.body.Body;
import io.github.wasabithumb.xpdy.payload.cookie.Cookie;
import io.github.wasabithumb.xpdy.payload.cookie.Cookies;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@ApiStatus.Internal
record ResponseImpl(
        @Range(from=100, to=599) int code,
        @NotNull Body body,
        @NotNull Headers headers,
        @NotNull Cookies cookies
) implements Response {

    static final class Builder implements Response.Builder {

        private int code = 200;
        private Body body = null;
        private final Headers headers = new Headers();
        private final Cookies cookies = new Cookies();

        //

        @Override
        public @NotNull Response.Builder code(@Range(from = 100, to = 599) int code) {
            if (code >= 400 && this.body == null) this.body(Body.error(code));
            this.code = code;
            return this;
        }

        @Override
        public @NotNull Response.Builder body(@NotNull Body body) {
            this.body = body;

            long size = body.size();
            if (size == -1L) {
                this.headers.remove("Content-Length");
            } else {
                this.headers.set("Content-Length", Long.toString(size));
            }
            this.headers.set("Content-Type", body.type());

            return this;
        }

        @Override
        public @NotNull Response.Builder addHeader(@NotNull String key, @NotNull String value) {
            this.headers.add(key, value);
            return this;
        }

        @Override
        public @NotNull Response.Builder setHeader(@NotNull String key, @NotNull String value) {
            this.headers.set(key, value);
            return this;
        }

        @Override
        public @NotNull Response.Builder setCookie(@NotNull Cookie cookie) {
            this.cookies.set(cookie);
            return this;
        }

        //

        @Override
        public @NotNull ResponseImpl build() {
            return new ResponseImpl(
                    this.code,
                    this.body == null ? Body.html("") : this.body,
                    this.headers,
                    this.cookies
            );
        }

    }

}
