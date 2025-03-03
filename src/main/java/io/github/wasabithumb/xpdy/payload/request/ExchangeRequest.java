package io.github.wasabithumb.xpdy.payload.request;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.github.wasabithumb.xpdy.except.BodyFormatException;
import io.github.wasabithumb.xpdy.misc.io.URLEncodedReader;
import io.github.wasabithumb.xpdy.payload.body.Body;
import io.github.wasabithumb.xpdy.payload.cookie.Cookies;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
final class ExchangeRequest implements Request {

    private final HttpExchange handle;
    private final Body body;
    private final Headers headers;
    private final Cookies cookies;
    private final List<String> pathParameters;
    private final Map<String, String> query;

    ExchangeRequest(@NotNull HttpExchange handle, @NotNull List<String> pathParameters) {
        Headers headers = handle.getRequestHeaders();
        Cookies cookies = new Cookies();
        cookies.read(headers, true, false);

        Map<String, String> query;
        try {
            query = URLEncodedReader.parse(handle.getRequestURI().getRawQuery());
        } catch (BodyFormatException e) {
            query = Collections.emptyMap();
        }

        this.handle         = handle;
        this.body           = Body.exchange(handle);
        this.headers        = headers;
        this.cookies        = cookies;
        this.pathParameters = Collections.unmodifiableList(pathParameters);
        this.query          = query;
    }

    //


    @Override
    public @NotNull Body body() {
        return this.body;
    }

    @Override
    public @NotNull Headers headers() {
        return this.headers;
    }

    @Override
    public @NotNull Cookies cookies() {
        return this.cookies;
    }

    @Override
    public @NotNull @Unmodifiable List<String> pathParameters() {
        return this.pathParameters;
    }

    @Override
    public @NotNull @Unmodifiable Map<String, String> query() {
        return this.query;
    }

    //

    @Override
    public int hashCode() {
        return this.handle.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof ExchangeRequest other) {
            return this.handle.equals(other.handle);
        }
        return super.equals(obj);
    }

}