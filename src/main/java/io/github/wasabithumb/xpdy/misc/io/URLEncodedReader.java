package io.github.wasabithumb.xpdy.misc.io;

import io.github.wasabithumb.xpdy.except.BodyFormatException;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class URLEncodedReader extends FilterReader {

    public static @NotNull @Unmodifiable Map<String, String> parse(@Nullable String string) throws BodyFormatException {
        if (string == null || string.isEmpty()) return Collections.emptyMap();
        try (StringReader sr = new StringReader(string);
             URLEncodedReader uer = new URLEncodedReader(sr)
        ) {
            return uer.readURLEncoded();
        } catch (IOException e) {
            throw new AssertionError("IOException should not be thrown by StringReader", e);
        }
    }

    //

    private long head = 0;
    public URLEncodedReader(@NotNull Reader in) {
        super(in);
    }

    //

    @Contract("_ -> fail")
    private void raiseParse(@NotNull String detail) throws BodyFormatException {
        throw new BodyFormatException(detail + " @ index " + this.head);
    }

    //

    @Override
    public int read() throws IOException {
        int r = super.read();
        if (r != -1) this.head++;
        return r;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int r = super.read(cbuf, off, len);
        if (r != -1) this.head += r;
        return r;
    }

    @Override
    public long skip(long n) throws IOException {
        long s = super.skip(n);
        this.head += s;
        return s;
    }

    //

    public @NotNull @Unmodifiable Map<String, String> readURLEncoded() throws IOException, BodyFormatException {
        Map<String, String> ret = new HashMap<>();
        String key = null;
        Token token;

        while ((token = this.readToken()) != null) {
            if (key == null) {
                switch (token.terminator) {
                    case EOF:
                    case AMP:
                        ret.put(token.content, "");
                        break;
                    case EQ:
                        key = token.content;
                        break;
                }
            } else {
                ret.put(key, token.content);
                switch (token.terminator) {
                    case EOF:
                    case AMP:
                        key = null;
                        break;
                    case EQ:
                        this.raiseParse("Unexpected equals (=) after value");
                        break;
                }
            }
        }

        if (key != null) {
            ret.put(key, "");
        }

        if (ret.isEmpty()) return Collections.emptyMap();
        return Collections.unmodifiableMap(ret);
    }

    private @Nullable Token readToken() throws IOException, BodyFormatException {
        StringBuilder sb = new StringBuilder();
        int c;

        while ((c = this.read()) != -1) {
            switch (c) {
                case '&':
                    return new Token(sb.toString(), Terminator.AMP);
                case '=':
                    if (sb.isEmpty())
                        this.raiseParse("Unexpected control character (=)");
                    return new Token(sb.toString(), Terminator.EQ);
                case '+':
                    sb.append(' ');
                    continue;
                case '%':
                    char[] hex = new char[2];
                    int hl = 0;
                    int hr;

                    do {
                        hr = this.read(hex, hl, 2 - hl);
                        if (hr == -1) this.raiseParse("Incomplete escape sequence");
                        hl += hr;
                    } while (hl != 2);

                    int ascii = 0;
                    for (int q=0; q < 2; q++) {
                        ascii <<= 4;
                        char hc = hex[q];
                        if ('0' <= hc && hc <= '9') {
                            ascii |= (hc - '0');
                        } else if ('A' <= hc && hc <= 'F') {
                            ascii |= (hc - 'A' + 10);
                        } else if ('a' <= hc && hc <= 'f') {
                            ascii |= (hc - 'a' + 10);
                        } else {
                            this.raiseParse("Invalid escape sequence");
                        }
                    }

                    if (ascii < 0x80) {
                        sb.append((char) ascii);
                    } else {
                        sb.append('�');
                    }
                    continue;
                case '!': case '#': case '$': case '\'':
                case '(': case ')': case '*': case ',':
                case '/': case ':': case ';': case '?':
                case '@': case '[': case ']':
                    this.raiseParse("Disallowed character: " + ((char) c));
                    break;
            }
            sb.append((char) c);
        }

        if (sb.isEmpty()) return null;
        return new Token(sb.toString(), Terminator.EOF);
    }

    //

    private record Token(
        @NotNull String content,
        @NotNull Terminator terminator
    ) { }

    private enum Terminator {
        /** End of stream */
        EOF,
        /** Ampersand ({@code &}) */
        AMP,
        /** Equals sign ({@code =}) */
        EQ
    }

}
