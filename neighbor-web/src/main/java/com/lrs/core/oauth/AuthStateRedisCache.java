package com.lrs.core.oauth;

import com.lrs.core.util.RedisUtil;
import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 扩展Redis版的state缓存
 * @author rstyro
 */
@Component
public class AuthStateRedisCache implements AuthStateCache {

    /**
     * 全局缓存key
     */
    public final static String AUTH_STATE_KEY = "global:auth_state:";

    private String buildKey(String key) {
        return AUTH_STATE_KEY + key;
    }

    /**
     * 存入缓存，默认3分钟
     *
     * @param key   缓存key
     * @param value 缓存内容
     */
    @Override
    public void cache(String key, String value) {
        RedisUtil.set(buildKey(key), value, AuthCacheConfig.timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 存入缓存
     *
     * @param key     缓存key
     * @param value   缓存内容
     * @param timeout 指定缓存过期时间（毫秒）
     */
    @Override
    public void cache(String key, String value, long timeout) {
        RedisUtil.set(buildKey(key), value, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取缓存内容
     *
     * @param key 缓存key
     * @return 缓存内容
     */
    @Override
    public String get(String key) {
        return RedisUtil.get(buildKey(key),String.class);
    }

    /**
     * 是否存在key，如果对应key的value值已过期，也返回false
     *
     * @param key 缓存key
     * @return true：存在key，并且value没过期；false：key不存在或者已过期
     */
    @Override
    public boolean containsKey(String key) {
        return RedisUtil.hasKey(buildKey(key));
    }
}

