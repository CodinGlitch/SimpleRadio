package com.codinglitch.simpleradio.lexiconfig.annotations;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LexiconEntry {
    String lang() default "";
    String path() default "";

    String comment() default "";
}
