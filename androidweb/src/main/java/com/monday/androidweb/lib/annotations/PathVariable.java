package com.monday.androidweb.lib.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Controller里的方法的参数，可以用@PathVariable 和 @RequestParam ，分别表示，路径上的参数和传过来的参数
 * Created by monday on 2017/8/10.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {

    String value() default "";          // 对应的路径中的变量名  ，例如 value="roomId"，就是指{roomId}

}
