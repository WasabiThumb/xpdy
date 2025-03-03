package io.github.wasabithumb.xpdy;

import io.github.wasabithumb.xpdy.endpoint.EndpointInject;
import io.github.wasabithumb.xpdy.endpoint.Endpoints;
import io.github.wasabithumb.xpdy.endpoint.methods.Get;
import io.github.wasabithumb.xpdy.endpoint.methods.Post;
import io.github.wasabithumb.xpdy.endpoint.methods.Put;
import io.github.wasabithumb.xpdy.payload.body.Body;
import io.github.wasabithumb.xpdy.payload.request.Request;
import io.github.wasabithumb.xpdy.payload.response.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TestEndpoints implements Endpoints {

    @EndpointInject
    private Instant start;

    @Get("/uptime")
    Response uptime() {
        long millis = Instant.now().toEpochMilli() - this.start.toEpochMilli();
        return Response.builder()
                .body(Body.text(Long.toString(millis)))
                .build();
    }

    @Post("/echo")
    Response echo(Request request) {
        return Response.builder()
                .body(request.body())
                .build();
    }

    @Get("/wildcard/*")
    Response wildcard(Request request) {
        return Response.builder()
                .body(Body.text(request.pathParameters().get(0)))
                .build();
    }

    @Put("/params")
    Response params(Request request) throws IOException {
        Map<String, String> map = request.urlencoded();
        String text = map.get("a") + ";" + map.get("b") + ";" + map.get("c");
        return Response.builder()
                .body(Body.text(text))
                .build();
    }

    @Get("/randimg")
    Response randimg() {
        int size = 512;
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Random r = ThreadLocalRandom.current();
        Graphics2D g = bi.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);

        for (int z=0; z < 128; z++) {
            g.setColor(new Color(r.nextInt(0x1000000)));
            g.setStroke(new BasicStroke(1f + r.nextFloat(8f)));
            g.drawLine(
                    r.nextInt(size),
                    r.nextInt(size),
                    r.nextInt(size),
                    r.nextInt(size)
            );
        }

        g.dispose();

        Body body = Body.builder()
                .type("image/jpeg")
                .source((OutputStream os) -> ImageIO.write(bi, "jpeg", os))
                .build();

        return Response.of(body);
    }

}
