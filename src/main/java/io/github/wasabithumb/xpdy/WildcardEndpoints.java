package io.github.wasabithumb.xpdy;

import io.github.wasabithumb.xpdy.endpoint.Endpoints;
import io.github.wasabithumb.xpdy.endpoint.methods.Get;
import io.github.wasabithumb.xpdy.payload.body.Body;
import io.github.wasabithumb.xpdy.payload.request.Request;
import io.github.wasabithumb.xpdy.payload.response.Response;

import java.util.List;

class WildcardEndpoints implements Endpoints {

    @Get("/sample/*/and/*")
    Response sample(Request request) {
        List<String> params = request.pathParameters();
        String text = params.get(0) + " and " + params.get(1);
        return Response.of(Body.text(text));
    }

}
