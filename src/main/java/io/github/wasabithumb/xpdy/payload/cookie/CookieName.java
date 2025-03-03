package io.github.wasabithumb.xpdy.payload.cookie;

import org.intellij.lang.annotations.Pattern;

import java.lang.annotation.*;

/**
 * Annotates an element to indicate that it should be a valid name for an HTTP cookie.
 * This is described by the "token" type of <a href="https://www.rfc-editor.org/rfc/rfc2616.html#section-2.2"> RFC 2616</a>.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Pattern("^[\\x21\\x23-\\x27\\x2A\\x2B\\x2D\\x2E\\x30-\\x39\\x41-\\x5A\\x5E-\\x7A\\x7C\\x7E]+$")
public @interface CookieName { }
