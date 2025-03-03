package io.github.wasabithumb.xpdy.endpoint.methods;

import io.github.wasabithumb.xpdy.misc.MimeType;
import io.github.wasabithumb.xpdy.misc.URIPath;

import java.lang.annotation.*;

/**
 * <p>
 *     Marks a method as an endpoint handler, accepting requests at the provided path
 *     with the HTTP <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/GET">GET</a> method.
 * </p>
 * <p>
 *     The {@code in()} parameter may NOT be specified. Unlike other methods,
 *     {@code GET} does not support request bodies.
 * </p>
 * <p>
 *     The {@link #out()} parameter may be specified to restrict the MIME type of the response body.
 *     An exception will be raised if the method provides a 2XX body of differing type.
 *     This also overrides the
 *     <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type">Content-Type</a> header.
 * </p>
 * @see io.github.wasabithumb.xpdy.endpoint.methods Endpoint Annotations
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Get {
    @URIPath String value();
    @MimeType String out() default "";
}
