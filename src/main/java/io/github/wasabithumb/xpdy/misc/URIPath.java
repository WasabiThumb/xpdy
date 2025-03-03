package io.github.wasabithumb.xpdy.misc;

import org.intellij.lang.annotations.Pattern;

import java.lang.annotation.*;

/**
 * Annotates an element to indicate that it should be a valid URI path with leading slash.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Pattern("^(?:\\/(?!\\/)(?:[\\w-.~!$&'()*+,;=:@]|%(?=[A-Fa-f\\d]{2}))*)+$")
public @interface URIPath { }
