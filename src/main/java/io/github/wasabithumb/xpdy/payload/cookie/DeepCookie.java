package io.github.wasabithumb.xpdy.payload.cookie;

import io.github.wasabithumb.xpdy.misc.Tristate;
import org.jetbrains.annotations.ApiStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;

@ApiStatus.Internal
record DeepCookie(
        String                          name,
        String                          value,
        Optional<String>                domain,
        Optional<Instant>               expires,
        Tristate                        httpOnly,
        OptionalInt                     maxAge,
        Tristate                        partitioned,
        Optional<String>                path,
        Optional<Cookie.SameSitePolicy> sameSite,
        Tristate                        secure
) implements Cookie { }
