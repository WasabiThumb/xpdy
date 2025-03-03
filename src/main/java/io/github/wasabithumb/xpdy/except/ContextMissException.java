package io.github.wasabithumb.xpdy.except;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Exception thrown when an endpoint method accepts a parameter of a type
 * which is not present in the {@link io.github.wasabithumb.xpdy.endpoint.EndpointContext EndpointContext}.
 * When this exception bubbles out of endpoint methods, HTTP 500 is served.
 */
public class ContextMissException extends ServeException {

    public ContextMissException(@NotNull Method method, @NotNull Parameter parameter) {
        super(500, "Endpoint method \"" + method.getName() + "\" in class " + method.getDeclaringClass().getName() +
                " accepts parameter \"" + parameter.getName() + "\" of type " + parameter.getType().getName() +
                " which is not present in the context");
    }

}
