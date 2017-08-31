package com.monday.androidweb.lib.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Controller里的方法，可以用 @RequestMapping 指派一个子路径， 例如 @RequestMapping("/add")
 * Created by monday on 2017/8/10.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    String value() default "";                // 访问的路径

    RequestMethod[] method() default {};        // 访问限制的方法

    ReturnType returnType() default ReturnType.JSON;        // 默认返回

}
