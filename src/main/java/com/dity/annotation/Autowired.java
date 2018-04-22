package com.dity.annotation;

import java.lang.annotation.*;

/**
 * @author:yuhang
 * @Date:2018/4/22
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD,ElementType.CONSTRUCTOR,ElementType.METHOD})
public @interface Autowired {
    public String value()default "";
}
