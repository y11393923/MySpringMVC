package com.zyy.annotation;

import java.lang.annotation.*;

@Documented     //指明修饰的注解，可以被例如javadoc此类的工具文档化，只负责标记，没有成员取值
@Target(ElementType.TYPE)   //注解只能用在类，接口或者枚举
@Retention(RetentionPolicy.RUNTIME)     //注解生命周期 运行级别保留
public @interface Controller {
    String value() default "";
}
