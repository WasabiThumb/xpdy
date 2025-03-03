package io.github.wasabithumb.xpdy.misc.io;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@ApiStatus.Internal
public class URLEncodedWriter extends FilterWriter {

    private static final long SPECIAL = 2882303768697806843L;
    private static boolean isSpecial(char c) {
        if (c < ' ' || c > ']') return false;
        return (SPECIAL & (1L << (c - ' '))) != 0;
    }

    public static byte @NotNull [] serializeUTF8(@NotNull Map<String, String> map) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
             URLEncodedWriter w = new URLEncodedWriter(osw)
        ) {
            w.writeURLEncoded(map);
            w.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new AssertionError("IOException thrown by ByteArrayOutputStream", e);
        }
    }

    //

    public URLEncodedWriter(@NotNull Writer out) {
        super(out);
    }

    //

    public void writeURLEncoded(@NotNull Map<String, String> map) throws IOException {
        boolean sep = false;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sep) this.write('&');
            sep = true;

            this.writeToken(entry.getKey());
            this.write('=');
            this.writeToken(entry.getValue());
        }
    }

    private void writeToken(@NotNull String token) throws IOException {
        int len = token.length();
        if (len == 0) return;

        int start = 0;
        char c;

        for (int i=0; i < len; i++) {
            c = token.charAt(i);
            if (!isSpecial(c)) continue;
            if (start != i)
                this.write(token, start, i - start);
            this.writeEscaped(c);
            start = i + 1;
        }

        if (start != len)
            this.write(token, start, len - start);
    }

    private void writeEscaped(char c) throws IOException {
        this.write('%');
        this.write(Character.forDigit(c >> 4, 16));
        this.write(Character.forDigit(c & 0xF, 16));
    }

}
