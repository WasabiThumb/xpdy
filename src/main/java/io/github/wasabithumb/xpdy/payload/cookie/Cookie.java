package io.github.wasabithumb.xpdy.payload.cookie;

import io.github.wasabithumb.xpdy.misc.Tristate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.time.Instant;
import java.util.*;

/**
 * An HTTP Cookie. A cookie is at minimum a name-value pair, with extra optional properties
 * as specified by <a href="https://httpwg.org/specs/rfc6265.html#sane-set-cookie">RFC 6265</a>.
 * Extra properties will not be present on incoming (serverbound) cookies.
 * @see #name()
 * @see #value()
 */
public interface Cookie {

    /**
     * <ul>
     *     <li>If {@code name} is null; returns null</li>
     *     <li>If {@code name} is non-null and is a valid name; returns {@code name}</li>
     *     <li>Otherwise, returns null</li>
     * </ul>
     */
    @Contract("null -> null")
    static @Nullable @CookieName String checkName(@Nullable String name) {
        //noinspection PatternValidation
        return CookieBuilderImpl.checkName(name);
    }

    /**
     * Creates a shallow cookie with the given name and value.
     */
    @Contract("_, _ -> new")
    static @NotNull Cookie of(@NotNull @CookieName String name, @NotNull String value) {
        return new ShallowCookie(name, value);
    }

    /**
     * Starts building a cookie.
     * The {@link Builder#name(String) name} must minimally be set in order for
     * {@link Builder#build() build()} to be called.
     */
    @Contract("-> new")
    static @NotNull Builder builder() {
        return new CookieBuilderImpl();
    }

    //

    /**
     * The name uniquely identifying this cookie.
     * Cookies with identical names will collide.
     */
    @NotNull @CookieName String name();

    /**
     * The value of this cookie.
     * Values with otherwise disallowed characters are
     * <a href="https://developer.mozilla.org/en-US/docs/Glossary/Percent-encoding">percent-encoded</a>
     * for transmission.
     */
    @NotNull String value();

    /**
     * <p>
     *     Returns true if this cookie is <i>shallow</i>, meaning that the
     *     {@link #name()} and {@link #value()} are known to completely identify this cookie.
     *     This may be used to skip expensive logic.
     * </p>
     * <p>
     *     This defaults to {@code false} and should only be {@code true} when it is definitely known
     *     that no other properties are set. A cookie is always permitted to be non-shallow even if it fits
     *     the criteria for shallowness.
     * </p>
     */
    default boolean isShallow() {
        return false;
    }

    /**
     * The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#domaindomain-value">Domain</a>
     * property of this cookie, if set.
     */
    default @NotNull Optional<String> domain() {
        return Optional.empty();
    }

    /**
     * The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#expiresdate">Expires</a>
     * property of this cookie, if set.
     */
    default @NotNull Optional<Instant> expires() {
        return Optional.empty();
    }

    /**
     * The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#httponly">HttpOnly</a> property
     * of this cookie.
     * <ul>
     *     <li>{@link Tristate#UNSET UNSET} - Not explicitly set</li>
     *     <li>
     *         {@link Tristate#FALSE FALSE} - Cookie is not {@link #isShallow() shallow}
     *         and value is explicitly set to false.
     *     </li>
     *     <li>
     *         {@link Tristate#TRUE TRUE} - Cookie is not {@link #isShallow() shallow}
     *         and value is explicitly set to true.
     *     </li>
     * </ul>
     */
    default @NotNull Tristate httpOnly() {
        return Tristate.UNSET;
    }

    /**
     * The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#max-agenumber">Max-Age</a>
     * property of this cookie, if set.
     */
    default @NotNull OptionalInt maxAge() {
        return OptionalInt.empty();
    }

    /**
     * The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#partitioned">Partitioned</a> property
     * of this cookie.
     * <ul>
     *     <li>{@link Tristate#UNSET UNSET} - Not explicitly set</li>
     *     <li>
     *         {@link Tristate#FALSE FALSE} - Cookie is not {@link #isShallow() shallow}
     *         and value is explicitly set to false.
     *     </li>
     *     <li>
     *         {@link Tristate#TRUE TRUE} - Cookie is not {@link #isShallow() shallow}
     *         and value is explicitly set to true.
     *     </li>
     * </ul>
     */
    default @NotNull Tristate partitioned() {
        return Tristate.UNSET;
    }

    /**
     * The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#pathpath-value">Path</a>
     * property of this cookie, if set.
     */
    default @NotNull Optional<String> path() {
        return Optional.empty();
    }

    /**
     * The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#samesitesamesite-value">Same-Site</a>
     * property of this cookie, if set.
     */
    default @NotNull Optional<SameSitePolicy> sameSite() {
        return Optional.empty();
    }

    /**
     * The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#secure">Secure</a> property
     * of this cookie.
     * <ul>
     *     <li>{@link Tristate#UNSET UNSET} - Not explicitly set</li>
     *     <li>
     *         {@link Tristate#FALSE FALSE} - Cookie is not {@link #isShallow() shallow}
     *         and value is explicitly set to false.
     *     </li>
     *     <li>
     *         {@link Tristate#TRUE TRUE} - Cookie is not {@link #isShallow() shallow}
     *         and value is explicitly set to true.
     *     </li>
     * </ul>
     */
    default @NotNull Tristate secure() {
        return Tristate.UNSET;
    }

    //

    /**
     * An allowed value for the {@link #sameSite() sameSite} parameter.
     * Use {@link #parse(String)} to obtain a {@link SameSitePolicy} from a {@link String}.
     */
    enum SameSitePolicy {
        STRICT,
        LAX,
        NONE;

        /**
         * Case-insensitive deserializer. For example, {@code "lax"} and {@code "Lax"} resolve to an Optional containing
         * {@link #LAX LAX}. For null or unrecognized values, {@link Optional#empty()} is returned.
         */
        public static @NotNull Optional<SameSitePolicy> parse(@Nullable String value) {
            if (value == null) return Optional.empty();
            int len = value.length();
            switch (len) {
                case 3:
                    if (lh(value, len) == 315452)
                        return Optional.of(LAX);
                    break;
                case 4:
                    if (lh(value, len) == 9851839)
                        return Optional.of(NONE);
                    break;
                case 6:
                    if (lh(value, len) == 1025572240)
                        return Optional.of(STRICT);
                    break;
            }
            return Optional.empty();
        }

        private static int lh(@NotNull CharSequence cs, int len) {
            int h = 7;
            for (int i=0; i < len; i++)
                h = 31 * h + (int) Character.toLowerCase(cs.charAt(i));
            return h;
        }

        public @NotNull String value() {
            char[] cs = this.name().toCharArray();
            for (int i=1; i < cs.length; i++)
                cs[i] = Character.toLowerCase(cs[i]);
            return new String(cs);
        }


        @Override
        public @NotNull String toString() {
            return this.value();
        }

    }

    //

    /**
     * Builds a non-{@link Cookie#isShallow() shallow} cookie.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    interface Builder {

        /**
         * Sets the {@link Cookie#name() name} of the cookie.
         */
        @Contract("_ -> this")
        @NotNull Builder name(@NotNull @CookieName String name);

        /**
         * Sets the {@link Cookie#value() value} of the cookie.
         */
        @Contract("_ -> this")
        @NotNull Builder value(@NotNull String value);

        /**
         * Sets the {@link Cookie#domain() domain} of the cookie.
         */
        @Contract("_ -> this")
        @NotNull Builder domain(@NotNull Optional<String> domain);

        /**
         * Sets the {@link Cookie#domain() domain} of the cookie.
         * @see #domain(Optional)
         */
        @Contract("_ -> this")
        default @NotNull Builder domain(@Nullable String domain) {
            return this.domain(Optional.ofNullable(domain));
        }

        /**
         * Sets the {@link Cookie#expires() expires} of the cookie.
         */
        @Contract("_ -> this")
        @NotNull Builder expires(@NotNull Optional<Instant> expires);

        /**
         * Sets the {@link Cookie#expires() expires} of the cookie.
         * @see #expires(Optional)
         */
        @Contract("_ -> this")
        default @NotNull Builder expires(@Nullable Instant expires) {
            return this.expires(Optional.ofNullable(expires));
        }

        /**
         * Sets the {@link Cookie#expires() expires} of the cookie
         * if the provided string is a valid <a href="https://datatracker.ietf.org/doc/html/rfc7231#section-7.1.1.1">RFC 7231</a> date.
         * @see #expires(Instant)
         */
        @Contract("_ -> this")
        default @NotNull Builder expires(@NotNull String expires) {
            try {
                return this.expires(CookieDate.parse(expires));
            } catch (ParseException e) {
                return this;
            }
        }

        /**
         * Sets the {@link Cookie#httpOnly() httpOnly} of the cookie.
         */
        @Contract("_ -> this")
        @NotNull Builder httpOnly(@NotNull Tristate httpOnly);

        /**
         * Sets the {@link Cookie#httpOnly() httpOnly} of the cookie.
         * @see #httpOnly(Tristate)
         */
        @Contract("_ -> this")
        default @NotNull Builder httpOnly(@Nullable Boolean httpOnly) {
            return this.httpOnly(Tristate.of(httpOnly));
        }

        /**
         * Sets the {@link Cookie#httpOnly() httpOnly} of the cookie.
         * @see #httpOnly(Tristate)
         */
        @Contract("_ -> this")
        default @NotNull Builder httpOnly(boolean httpOnly) {
            return this.httpOnly(Tristate.of(httpOnly));
        }

        /**
         * Sets the {@link Cookie#maxAge() maxAge} of the cookie.
         */
        @Contract("_ -> this")
        @NotNull Builder maxAge(@NotNull OptionalInt maxAge);

        /**
         * Sets the {@link Cookie#maxAge() maxAge} of the cookie.
         * @see #maxAge(OptionalInt)
         */
        default @NotNull Builder maxAge(@Nullable Integer maxAge) {
            return this.maxAge(maxAge == null ? OptionalInt.empty() : OptionalInt.of(maxAge));
        }

        /**
         * Sets the {@link Cookie#maxAge() maxAge} of the cookie.
         * @see #maxAge(OptionalInt)
         */
        default @NotNull Builder maxAge(int maxAge) {
            return this.maxAge(OptionalInt.of(maxAge));
        }

        /**
         * Sets the {@link Cookie#maxAge() maxAge} of the cookie, if the provided string is a valid integer.
         * @see #maxAge(OptionalInt)
         */
        default @NotNull Builder maxAge(@NotNull String maxAge) {
            try {
                return this.maxAge(Integer.parseInt(maxAge));
            } catch (NumberFormatException e) {
                return this;
            }
        }

        /**
         * Sets the {@link Cookie#partitioned() partitioned} of the cookie.
         */
        @Contract("_ -> this")
        @NotNull Builder partitioned(@NotNull Tristate partitioned);

        /**
         * Sets the {@link Cookie#partitioned() partitioned} of the cookie.
         * @see #partitioned(Tristate)
         */
        @Contract("_ -> this")
        default @NotNull Builder partitioned(@Nullable Boolean partitioned) {
            return this.partitioned(Tristate.of(partitioned));
        }

        /**
         * Sets the {@link Cookie#partitioned() partitioned} of the cookie.
         * @see #partitioned(Tristate)
         */
        @Contract("_ -> this")
        default @NotNull Builder partitioned(boolean partitioned) {
            return this.partitioned(Tristate.of(partitioned));
        }

        /**
         * Sets the {@link Cookie#path() path} of the cookie.
         */
        @Contract("_ -> this")
        @NotNull Builder path(@NotNull Optional<String> path);

        /**
         * Sets the {@link Cookie#path() path} of the cookie.
         * @see #path(Optional)
         */
        @Contract("_ -> this")
        default @NotNull Builder path(@Nullable String path) {
            return this.path(Optional.ofNullable(path));
        }

        /**
         * Sets the {@link Cookie#sameSite() sameSite} of the cookie.
         */
        @NotNull Builder sameSite(@NotNull Optional<SameSitePolicy> sameSite);

        /**
         * Sets the {@link Cookie#sameSite() sameSite} of the cookie.
         * @see #sameSite(Optional)
         */
        default @NotNull Builder sameSite(@Nullable SameSitePolicy sameSite) {
            return this.sameSite(Optional.ofNullable(sameSite));
        }

        /**
         * Sets the {@link Cookie#sameSite() sameSite} of the cookie, if the
         * provided string is a {@link SameSitePolicy#parse(String) valid SameSite value}.
         * @see #sameSite(Optional)
         */
        default @NotNull Builder sameSite(@Nullable String sameSite) {
            return this.sameSite(SameSitePolicy.parse(sameSite));
        }

        /**
         * Sets the {@link Cookie#secure() secure} of the cookie.
         */
        @Contract("_ -> this")
        @NotNull Builder secure(@NotNull Tristate secure);

        /**
         * Sets the {@link Cookie#secure() secure} of the cookie.
         * @see #secure(Tristate)
         */
        @Contract("_ -> this")
        default @NotNull Builder secure(@Nullable Boolean secure) {
            return this.secure(Tristate.of(secure));
        }

        /**
         * Sets the {@link Cookie#secure() secure} of the cookie.
         * @see #secure(Tristate)
         */
        @Contract("_ -> this")
        default @NotNull Builder secure(boolean secure) {
            return this.secure(Tristate.of(secure));
        }

        /**
         * Replaces the state of this builder with the values
         * stored in the provided cookie.
         */
        @Contract("_ -> this")
        default @NotNull Builder set(@NotNull Cookie cookie) {
            //noinspection PatternValidation
            return this.name(cookie.name())
                    .value(cookie.value())
                    .domain(cookie.domain())
                    .expires(cookie.expires())
                    .httpOnly(cookie.httpOnly())
                    .maxAge(cookie.maxAge())
                    .partitioned(cookie.partitioned())
                    .path(cookie.path())
                    .sameSite(cookie.sameSite())
                    .secure(cookie.secure());
        }

        /**
         * Builds a new cookie from the internal state of the builder.
         * @throws IllegalStateException Builder is missing a required property;
         *                               {@link #name(String) name} is not set
         */
        @Contract("-> new")
        @NotNull Cookie build() throws IllegalStateException;

    }

}
