package com.lrs.core;

import com.lrs.core.util.RedisUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.UUID;

@SpringBootTest
public class RedisUtilTest {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User{
        private String username;
        private int age;
    }



    @Test
    public void testUser(){
        User user = User.builder().username("rstyro").age(18).build();

        RedisUtil.set("user", user);
        User redisUser = RedisUtil.get("user", User.class);
        System.out.println("redisUser="+redisUser);
    }

    @Test
    public void testSimple(){
        // 字符串操作
        RedisUtil.set("user:1", "张三");
        RedisUtil.set("user:1", "张三", 300); // 5分钟过期
        String userName = RedisUtil.get("user:1", String.class);
        System.out.println("userName="+userName);


// 分布式锁
        String lockKey = "order:lock:123";
        String requestId = UUID.randomUUID().toString();
        if (RedisUtil.tryLock(lockKey, requestId, 30)) {
            try {
                // 执行业务逻辑
            } finally {
                RedisUtil.releaseLock(lockKey, requestId);
            }
        }

// 计数器
        Long count = RedisUtil.incr("page:view:home", 1);
        System.out.println("count="+count);

// 集合操作
        RedisUtil.sSet("user:tags:1", "VIP", "NEW_USER", "ACTIVE");

// 列表操作
        RedisUtil.lSet("message:queue", "消息1");
        RedisUtil.lSet("message:queue", Arrays.asList("消息2", "消息3"));

        System.out.println("message:queue="+RedisUtil.lGetIndex("message:queue",1));
        System.out.println("message:queue="+RedisUtil.lGetIndex("message:queue",2));
    }
}
