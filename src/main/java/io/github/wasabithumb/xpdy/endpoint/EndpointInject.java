package io.github.wasabithumb.xpdy.endpoint;

import java.lang.annotation.*;

/**
 * Can be used to decorate a constructor or field in an {@link Endpoints endpoint class} to mark it
 * for injection. Similar to Guice's <a href="https://github.com/google/guice/wiki/Injections">@Inject</a>.
 */
@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EndpointInject { }
