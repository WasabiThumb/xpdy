package io.github.wasabithumb.xpdy.payload.cookie;

import com.sun.net.httpserver.Headers;
import io.github.wasabithumb.xpdy.except.MissingCookieException;
import io.github.wasabithumb.xpdy.misc.Tristate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

/**
 * A cookie jar, which can:
 * <ul>
 *     <li>Be read</li>
 *     <li>Be written to</li>
 *     <li>Read {@code Cookie} headers from {@link Headers} into internal state</li>
 *     <li>Write {@code Set-Cookie} headers to {@link Headers} from internal state</li>
 * </ul>
 * @see #read(Map)
 * @see #write(Map)
 * @see #getValueAssert(String)
 * @see #setValue(String, String)
 * @see #set(Cookie)
 */
public final class Cookies {

    private final Map<String, Cookie> map;

    public Cookies(int initialCapacity) {
        this.map = new HashMap<>(initialCapacity);
    }

    public Cookies() {
        this.map = new HashMap<>();
    }

    //

    /**
     * Clears all stored cookies.
     */
    public void clear() {
        this.map.clear();
    }

    /**
     * Returns a set containing all stored cookie names.
     */
    public @NotNull @Unmodifiable Set<String> names() {
        return Set.copyOf(this.map.keySet());
    }

    //

    /**
     * Gets the cookie with the given name, if any.
     * If {@code ignoreCase} is {@code true}, the first cookie
     * that equals {@code name} with semantics as defined by {@link String#equalsIgnoreCase(String)}
     * is returned.
     */
    public @Nullable Cookie get(@NotNull String name, boolean ignoreCase) {
        Cookie ret = this.map.get(name);
        if (ret != null) return ret;
        if (ignoreCase) {
            for (Map.Entry<String, Cookie> entry : this.map.entrySet()) {
                if (name.equalsIgnoreCase(entry.getKey())) return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Non-nullable variant of {@link #get(String, boolean)}
     * @throws MissingCookieException Cookie is not set
     */
    public @NotNull Cookie getAssert(@NotNull String name, boolean ignoreCase) throws MissingCookieException {
        return this.checkMissing(this.get(name, ignoreCase), name);
    }

    /**
     * Gets the cookie with the given name, if any. Alias for {@code get(name, false)}.
     * @see #get(String, boolean)
     */
    public @Nullable Cookie get(@NotNull String name) {
        return this.get(name, false);
    }

    /**
     * Non-nullable variant of {@link #get(String)}
     * @throws MissingCookieException Cookie is not set
     */
    public @NotNull Cookie getAssert(@NotNull String name) throws MissingCookieException {
        return this.checkMissing(this.get(name), name);
    }

    /**
     * Gets the value of the cookie with the given name, if any.
     * If {@code ignoreCase} is {@code true}, the value of the first cookie
     * that equals {@code name} with semantics as defined by {@link String#equalsIgnoreCase(String)}
     * is returned.
     * @see #get(String, boolean)
     */
    public @Nullable String getValue(@NotNull String name, boolean ignoreCase) {
        Cookie c = this.get(name, ignoreCase);
        return c == null ? null : c.value();
    }

    /**
     * Non-nullable variant of {@link #getValue(String, boolean)}
     * @throws MissingCookieException Cookie is not set
     */
    public @NotNull String getValueAssert(@NotNull String name, boolean ignoreCase) throws MissingCookieException {
        return this.checkMissing(this.getValue(name, ignoreCase), name);
    }

    /**
     * Gets the value of the cookie with the given name, if any. Alias for {@code getValue(name, false)}.
     * @see #getValue(String, boolean)
     */
    public @Nullable String getValue(@NotNull String name) {
        return this.getValue(name, false);
    }

    /**
     * Non-nullable variant of {@link #getValue(String)}
     * @throws MissingCookieException Cookie is not set
     */
    public @NotNull String getValueAssert(@NotNull String name) throws MissingCookieException {
        return this.checkMissing(this.getValue(name), name);
    }

    //

    /**
     * Updates a cookie. If a cookie with the same name as the given cookie is already set,
     * that cookie is overwritten.
     */
    public void set(@NotNull Cookie cookie) {
        this.map.put(cookie.name(), cookie);
    }

    /**
     * Sets the value associated with a given name.
     * If a cookie already exists, the value is updated.
     * If a cookie does not exist, {@code generator} is called to create a cookie.
     */
    public void setValue(
            @NotNull @CookieName String name,
            @NotNull String value,
            @NotNull Supplier<? extends Cookie.Builder> generator
    ) {
        this.map.compute(
                name,
                (String ignored, Cookie existing) -> {
                    Cookie.Builder cb;
                    if (existing == null) {
                        cb = generator.get().name(name);
                    } else {
                        cb = Cookie.builder().set(existing);
                    }
                    return cb.value(value).build();
                }
        );
    }

    /**
     * Sets the value associated with a given name.
     * If a cookie already exists, the value is updated.
     * Alias for {@code setValue(name, value, Cookie::builder)}.
     * @see #setValue(String, String, Supplier)
     */
    public void setValue(
            @NotNull @CookieName String name,
            @NotNull String value
    ) {
        this.setValue(name, value, Cookie::builder);
    }

    //

    /**
     * Reads cookies into internal state
     * @param headers Headers to read cookie data from
     * @param readCookie If {@code Cookie} headers should be read
     * @param readSetCookie If {@code Set-Cookie} headers should be read
     */
    @Contract("_, false, false -> fail")
    public void read(
            @NotNull Map<String, ? extends Collection<? extends String>> headers,
            boolean readCookie,
            boolean readSetCookie
    ) throws IllegalArgumentException {
        if (readCookie) {
            if (readSetCookie)
                this.readSetCookie(headers);
            this.readCookie(headers);
        } else if (readSetCookie) {
            this.readSetCookie(headers);
        } else {
            throw new IllegalArgumentException("One of readCookie, readSetCookie must be true");
        }
    }

    /**
     * Reads cookies into internal state. Alias for {@code read(headers, true, true)}.
     * @param headers Headers to read cookie data from
     * @see #read(Map, boolean, boolean)
     */
    public void read(@NotNull Map<String, ? extends Collection<? extends String>> headers) {
        this.read(headers, true, true);
    }

    private void readCookie(@NotNull Map<String, ? extends Collection<? extends String>> headers) {
        Iterable<? extends String> list = headers.get("Cookie");
        if (list == null) return;

        for (String str : list) {
            int len = str.length();
            if (len == 0) continue;

            int start = 0;
            for (int i=0; i < len; i++) {
                if (str.charAt(i) != ';') continue;
                int a = start;
                start = i + 1;
                if (start < len && str.charAt(start) == ' ') start++;
                if (i != a) this.readCookieSingle(str.substring(a, i));
            }
            if (start != len)
                this.readCookieSingle(str.substring(start));
        }
    }

    private void readCookieSingle(@NotNull String single) {
        int whereEq = single.indexOf('=');
        String name, value;
        if (whereEq == -1) {
            name = single;
            value = "";
        } else {
            name = single.substring(0, whereEq);
            value = single.substring(whereEq + 1);
        }

        name = Cookie.checkName(name);
        if (name == null) return;
        value = URLDecoder.decode(value, StandardCharsets.UTF_8);

        //noinspection PatternValidation
        Cookie c = Cookie.of(
                name,
                value
        );
        this.set(c);
    }

    private void readSetCookie(@NotNull Map<String, ? extends Collection<? extends String>> headers) {
        Iterable<? extends String> list = headers.get("Set-Cookie");
        if (list == null) return;

        for (String str : list) {
            int len = str.length();
            if (len == 0) continue;

            int kvEnd = str.indexOf(';');
            if (kvEnd == -1) kvEnd = len;

            //noinspection PatternValidation
            int eq = str.indexOf('=');
            if (eq < 1 || eq >= kvEnd) continue;

            //noinspection PatternValidation
            String name = Cookie.checkName(str.substring(0, eq));
            if (name == null) continue;
            String value = URLDecoder.decode(str.substring(eq + 1, kvEnd), StandardCharsets.UTF_8);

            Cookie.Builder builder = Cookie.builder()
                    .name(name)
                    .value(value);

            boolean trim = true;
            int propStart = kvEnd + 1;
            int i = propStart;
            char c;

            for (; i < len; i++) {
                c = str.charAt(i);
                if (trim) {
                    if (c == ' ') {
                        propStart = i + 1;
                        continue;
                    } else {
                        trim = false;
                    }
                }
                if (c == ';') {
                    int a = propStart;
                    propStart = i + 1;
                    trim = true;
                    if (a != i) this.readSetCookieProperty(builder, str.substring(a, i));
                }
            }
            if (propStart < len)
                this.readSetCookieProperty(builder, str.substring(propStart));

            this.set(builder.build());
        }
    }

    private void readSetCookieProperty(@NotNull Cookie.Builder builder, @NotNull String prop) {
        int eq = prop.indexOf('=');
        switch (eq) {
            case -1:
                switch (prop) {
                    case "HttpOnly" -> builder.httpOnly(Tristate.TRUE);
                    case "Partitioned" -> builder.partitioned(Tristate.TRUE);
                    case "Secure" -> builder.secure(Tristate.TRUE);
                }
                break;
            case 4:
                if (prop.startsWith("Path"))
                    builder.path(prop.substring(5));
                break;
            case 6:
                if (prop.startsWith("Domain"))
                    builder.domain(prop.substring(7));
                break;
            case 7:
                if (prop.startsWith("Expires")) {
                    builder.expires(prop.substring(8));
                } else if (prop.startsWith("Max-Age")) {
                    builder.maxAge(prop.substring(8));
                }
                break;
            case 8:
                if (prop.startsWith("SameSite"))
                    builder.sameSite(prop.substring(9));
                break;
        }
    }

    //

    /**
     * Writes the content of this cookie jar as a list of {@code Set-Cookie} headers into the provided
     * {@link Headers}. Any existing {@code Set-Cookie} headers will be overwritten.
     */
    public void write(@NotNull Map<String, List<String>> headers) {
        int size = this.map.size();
        if (size == 0) {
            headers.remove("Set-Cookie");
            return;
        }

        List<String> ret = new ArrayList<>(size);
        for (Cookie c : this.map.values()) {
            ret.add(this.writeSingle(c));
        }

        headers.put("Set-Cookie", ret);
    }

    private @NotNull String writeSingle(@NotNull Cookie cookie) {
        StringBuilder sb = new StringBuilder(cookie.name());
        sb.append('=').append(URLEncoder.encode(cookie.value(), StandardCharsets.UTF_8));

        if (cookie.isShallow())
            return sb.toString();

        this.writeSingleProperties(cookie, sb);
        return sb.toString();
    }

    private void writeSingleProperties(@NotNull Cookie c, @NotNull StringBuilder sb) {
        c.domain().ifPresent((String domain) -> sb.append("; Domain=").append(domain));
        c.expires().ifPresent((Instant expires) -> sb.append("; Expires=").append(CookieDate.format(expires)));
        c.maxAge().ifPresent((int maxAge) -> sb.append("; Max-Age=").append(maxAge));
        c.path().ifPresent((String path) -> sb.append("; Path=").append(path));
        c.sameSite().ifPresent((Cookie.SameSitePolicy ssp) -> sb.append("; SameSite=").append(ssp.value()));
        if (c.httpOnly().valueOr(false)) sb.append("; HttpOnly");
        if (c.partitioned().valueOr(false)) sb.append("; Partitioned");
        if (c.secure().valueOr(false)) sb.append("; Secure");
    }

    //

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();
        boolean sep = false;

        sb.append("Cookies[");

        for (Cookie c : this.map.values()) {
            if (sep) sb.append(", ");
            sep = true;

            sb.append(c.name())
                    .append('=')
                    .append(c.value());
        }

        return sb.append(']')
                .toString();
    }

    //

    @Contract("null, _ -> fail; !null, _ -> param1")
    private <T> @NotNull T checkMissing(@Nullable T cookie, @NotNull String name) throws MissingCookieException {
        if (cookie == null)
            throw new MissingCookieException(name);
        return cookie;
    }

}
