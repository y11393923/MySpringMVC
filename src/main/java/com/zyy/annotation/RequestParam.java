package com.zyy.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)      //注解只能用在参数上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value();
}
