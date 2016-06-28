package io.burt.jmespath.function;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;
import java.lang.annotation.Documented;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
@interface Function {
  String name() default "";
  int minArity() default -1;
  int maxArity() default -1;
  int arity() default -1;
}
