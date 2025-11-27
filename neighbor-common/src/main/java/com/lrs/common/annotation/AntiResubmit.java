package com.lrs.common.annotation;

import com.lrs.common.enums.LockType;

import java.lang.annotation.*;

/**
 * 防重复提交注解
 * 使用场景：防止用户短时间内重复提交表单/请求
 * @author rstyro
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AntiResubmit {

    /**
     * 锁定的key，支持SpEL表达式
     * 示例: #user.id, #requestParam
     */
    String key() default "";

    /**
     * 锁定的时间（秒）
     * 默认3秒，根据业务调整
     */
    int lockTime() default 3;

    /**
     * 错误提示信息
     */
    String message() default "请勿重复提交";

    /**
     * 是否包含用户信息
     * 为true时自动拼接用户ID到key中
     */
    boolean includeUser() default true;

    /**
     * 锁类型
     */
    LockType lockType() default LockType.PARAM;


}
