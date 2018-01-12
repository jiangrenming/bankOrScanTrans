package com.nld.thirdlibrary;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jiangrenming on 2017/12/27.
 * 用于注解 Activity 跳转所需的参数, 通过它来生成最终的跳转 URI
 */

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UriParams {

    String value();
}
