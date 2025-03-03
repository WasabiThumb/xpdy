package io.github.wasabithumb.xpdy;

import io.github.wasabithumb.xpdy.logging.XpdyLogger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class XpdyServerTest {

    private static XpdyServer SERVER = null;

    @BeforeAll
    static void setup() throws IOException {
        XpdyServer server = XpdyServer.builder()
                .port(9739)
                .logger(XpdyLogger.simple())
                .inject(Instant.class, Instant.now())
                .build();

        server.registerEndpoints(TestEndpoints.class);
        server.start();

        SERVER = server;
    }

    @AfterAll
    static void cleanup() {
        if (SERVER != null) SERVER.stop(0);
    }

    //

    @Test
    void uptime() throws IOException {
        HttpURLConnection connection = this.open("/uptime");
        String uptime = this.readText(connection);
        assertTrue(Long.parseLong(uptime) >= 0L);
    }

    @Test
    void echo() throws IOException {
        byte[] buf = new byte[8192];
        ThreadLocalRandom.current().nextBytes(buf);

        HttpURLConnection connection = this.open("/echo");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(buf);
            os.flush();
        }

        int head = 0;
        int read;
        try (InputStream is = connection.getInputStream()) {
            while ((read = is.read()) != -1) {
                assertEquals(buf[head++], (byte) read);
            }
        }
        assertEquals(buf.length, head);
    }

    @Test
    void wildcard() throws IOException {
        String nonce = this.generateNonce();
        HttpURLConnection connection = this.open("/wildcard/" + nonce);
        String response = this.readText(connection);
        assertEquals(nonce, response);
    }

    @Test
    void params() throws IOException {
        String a = this.generateNonce();
        String b = this.generateNonce();
        String c = this.generateNonce();

        HttpURLConnection connection = this.open("/params?a=" + a);
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            osw.write("b=");
            osw.write(b);
            osw.write("&c=");
            osw.write(c);
        }

        String response = this.readText(connection);
        assertEquals(a + ";" + b + ";" + c, response);
    }

    @Test
    void randimg() throws IOException {
        HttpURLConnection connection = this.open("/randimg");
        BufferedImage img;
        try (InputStream is = connection.getInputStream()) {
            img = ImageIO.read(is);
        }
        assertEquals(512, img.getWidth());
        assertEquals(512, img.getHeight());
    }

    //

    private @NotNull HttpURLConnection open(@NotNull String path) throws IOException {
        URL url = URI.create("http://127.0.0.1:9739" + path).toURL();
        return (HttpURLConnection) url.openConnection();
    }

    private @NotNull String readText(@NotNull HttpURLConnection connection) throws IOException {
        try (InputStream is = connection.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)
        ) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int read;

            while ((read = isr.read(buf)) != -1)
                sb.append(buf, 0, read);

            return sb.toString();
        }
    }

    private @NotNull String generateNonce() {
        Random random = ThreadLocalRandom.current();
        char[] chars = new char[16];
        int r;
        for (int i = 0; i < 16; i++) {
            r = random.nextInt(16);
            if (r < 10) {
                chars[i] = (char) ('0' + r);
            } else {
                chars[i] = (char) ('A' + r - 10);
            }
        }
        return new String(chars);
    }

}