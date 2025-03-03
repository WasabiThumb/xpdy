package io.github.wasabithumb.xpdy.misc;

import org.intellij.lang.annotations.Pattern;

import java.lang.annotation.*;

/**
 * Annotates an element to indicate that it should be a valid MIME type.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Pattern("^(?:application|audio|font|haptics|image|message|model|multipart|text|video)\\/[\\w-+.]+$")
public @interface MimeType { }
