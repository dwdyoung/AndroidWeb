package com.gonsin.androidweb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by monday on 2017/8/10.
 */
//自定义注解  @interface  默认继承Annotation
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

    /**
     * Controller文件对应的根路径
     * @return
     */
    String value() default "";

}
