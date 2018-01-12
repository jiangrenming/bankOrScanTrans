package com.nld.thirdlibrary;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jiangrenming on 2017/12/27.
 * *组合规则:
 * <scheme>://<host>:<port>/<path>
 * 例如:
 * openAnjukeApp://com.baronzhang.android.im:6666/im/home
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CombinationUri {

    String scheme();

    String host();

    String port() default "";

    String path() default "";
}
