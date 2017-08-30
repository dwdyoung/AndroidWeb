package com.gonsin.androidweb.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Controller里的方法的参数，可以用@PathVariable 和 @RequestParam ，分别表示，路径上的参数和传过来的参数
 * Created by monday on 2017/8/10.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    String value() default "";          // 参数名字

    boolean required() default true;        // 是否必须

    String defaultValue() default "\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n";   // 如果不是必须，则默认值是

}
