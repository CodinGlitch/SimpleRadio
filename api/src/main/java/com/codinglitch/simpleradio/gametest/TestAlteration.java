package com.codinglitch.simpleradio.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestAlteration {
    String id() default "";
    String name() default "";
    String templateDir() default "";
}
