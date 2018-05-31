package com.autoConfig.client.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoConfig {
	// 配置key
	String key() default "";
	
	// 所在项目ID
	String groupId() default "";
	
	// 注入默认值
	String defaultValue() default "";
}
