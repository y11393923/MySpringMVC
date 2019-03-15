package com.zyy.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})     //注解能用于类，接口，枚举和方法
@Documented
public @interface RequestMapping {
    String value() default "";
}
