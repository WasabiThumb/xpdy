package io.github.wasabithumb.xpdy;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
final class XpdyBuildInfo {

    private static final Pattern PATTERN = Pattern.compile("^([a-z\\-]+):\\x20?(.*)$", Pattern.CASE_INSENSITIVE);
    private static Map<String, String> MAP = null;

    //

    public static @Nullable String get(@NotNull String key) {
        Map<String, String> map;
        synchronized (XpdyBuildInfo.class) {
            map = MAP;
            if (map == null) {
                try {
                    MAP = map = load();
                } catch (IOException ignored) {
                    return null;
                }
            }
        }
        return map.get(key.toLowerCase(Locale.ROOT));
    }

    private static @NotNull @Unmodifiable Map<String, String> load() throws IOException {
        Map<String, String> ret = new HashMap<>(4, 1f);
        try (InputStream is = XpdyBuildInfo.class.getResourceAsStream("/META-INF/xpdy/BUILD.txt")) {
            if (is == null) throw new IOException("Could not find META-INF/xpdy/BUILD.txt");
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)
            ) {
                String line;
                Matcher m;
                while ((line = br.readLine()) != null) {
                    m = PATTERN.matcher(line);
                    if (!m.matches()) continue;
                    ret.put(
                            m.group(1).toLowerCase(Locale.ROOT),
                            m.group(2)
                    );
                }
            }
        }
        return Collections.unmodifiableMap(ret);
    }

}
