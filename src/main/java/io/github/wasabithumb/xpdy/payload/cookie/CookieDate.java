package io.github.wasabithumb.xpdy.payload.cookie;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@ApiStatus.Internal
final class CookieDate {

    private static final SimpleDateFormat FORMAT;
    static {
        FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    //

    public static @NotNull Instant parse(@NotNull String date) throws ParseException {
        return FORMAT.parse(date).toInstant();
    }

    public static @NotNull String format(@NotNull Instant instant) {
        return FORMAT.format(Date.from(instant));
    }

}
