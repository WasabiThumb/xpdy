package io.github.wasabithumb.xpdy.payload.cookie;

import io.github.wasabithumb.xpdy.misc.Tristate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ApiStatus.Internal
final class CookieBuilderImpl implements Cookie.Builder {

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[\\x21\\x23-\\x27\\x2A\\x2B\\x2D\\x2E\\x30-\\x39\\x41-\\x5A\\x5E-\\x7A\\x7C\\x7E]+$"
    );

    @Contract("null -> null")
    static @Nullable @CookieName String checkName(@Nullable String name) {
        if (name == null) return null;
        if (!NAME_PATTERN.matcher(name).matches()) return null;
        //noinspection PatternValidation
        return name;
    }

    //

    private String                          name        = null;
    private String                          value       = "";
    private boolean                         shallow     = true;
    private Optional<String>                domain      = Optional.empty();
    private Optional<Instant>               expires     = Optional.empty();
    private Tristate                        httpOnly    = Tristate.UNSET;
    private OptionalInt                     maxAge      = OptionalInt.empty();
    private Tristate                        partitioned = Tristate.UNSET;
    private Optional<String>                path        = Optional.empty();
    private Optional<Cookie.SameSitePolicy> sameSite    = Optional.empty();
    private Tristate                        secure      = Tristate.UNSET;

    //

    @Override
    public @NotNull Cookie.Builder name(@NotNull String name) {
        if (checkName(name) == null)
            throw new IllegalArgumentException("Cookie name \"" + name + "\" is invalid");
        this.name = name;
        return this;
    }

    @Override
    public @NotNull Cookie.Builder value(@NotNull String value) {
        this.value = value;
        return this;
    }

    @Override
    public @NotNull Cookie.Builder domain(@NotNull Optional<String> domain) {
        this.domain = domain;
        this.shallow = false;
        return this;
    }

    @Override
    public @NotNull Cookie.Builder expires(@NotNull Optional<Instant> expires) {
        this.expires = expires;
        this.shallow = false;
        return this;
    }

    @Override
    public @NotNull Cookie.Builder httpOnly(@NotNull Tristate httpOnly) {
        this.httpOnly = httpOnly;
        this.shallow = false;
        return this;
    }

    @Override
    public @NotNull Cookie.Builder maxAge(@NotNull OptionalInt maxAge) {
        this.maxAge = maxAge;
        this.shallow = false;
        return this;
    }

    @Override
    public @NotNull Cookie.Builder partitioned(@NotNull Tristate partitioned) {
        this.partitioned = partitioned;
        this.shallow = false;
        return this;
    }

    @Override
    public @NotNull Cookie.Builder path(@NotNull Optional<String> path) {
        this.path = path;
        this.shallow = false;
        return this;
    }

    @Override
    public @NotNull Cookie.Builder sameSite(@NotNull Optional<Cookie.SameSitePolicy> sameSite) {
        this.sameSite = sameSite;
        this.shallow = false;
        return this;
    }

    @Override
    public @NotNull Cookie.Builder secure(@NotNull Tristate secure) {
        this.secure = secure;
        this.shallow = false;
        return this;
    }

    @Override
    public @NotNull Cookie.Builder set(@NotNull Cookie cookie) {
        Cookie.Builder.super.set(cookie);
        if (cookie.isShallow()) this.shallow = true;
        return this;
    }

    //

    @Override
    public @NotNull Cookie build() throws IllegalStateException {
        String name = this.name;
        if (name == null)
            throw new IllegalStateException("Cannot create cookie without a name");

        if (this.shallow)
            return new ShallowCookie(this.name, this.value);

        return new DeepCookie(
                this.name,
                this.value,
                this.domain,
                this.expires,
                this.httpOnly,
                this.maxAge,
                this.partitioned,
                this.path,
                this.sameSite,
                this.secure
        );
    }

}
