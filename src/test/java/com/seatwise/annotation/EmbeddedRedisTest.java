package com.seatwise.annotation;

import com.seatwise.config.RedisTestConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RedisTestConfig.class)
public @interface EmbeddedRedisTest {}
