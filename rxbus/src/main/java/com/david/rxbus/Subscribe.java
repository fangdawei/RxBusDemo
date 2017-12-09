package com.david.rxbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by david on 2017/5/13.
 */

@Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface Subscribe {
  ThreadMode thread() default ThreadMode.CURRENT;
}
