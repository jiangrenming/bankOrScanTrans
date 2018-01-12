package com.nld.thirdlibrary;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jiangrenming on 2017/12/27.
 * 注解 Activity 跳转方法
 * 跳转时的完整路径
 * 优先级高于CommbinationUri
 * 若两者同时出现在一个跳转中，则CommbinationUri被忽略
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FullUri {

    String value();
}
