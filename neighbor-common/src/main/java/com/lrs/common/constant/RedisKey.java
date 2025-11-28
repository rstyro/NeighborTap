package com.lrs.common.constant;

/**
 * Redis 缓存key
 */
public interface RedisKey {

    /**
     * 防重提交 redis key
     */
    String REPEAT_SUBMIT_KEY = "repeat_submit:lock:";

    /**
     * 用户登录报错key
     */
    String USER_ACCOUNT_ERR_KEY = "user:account:err:";
}
