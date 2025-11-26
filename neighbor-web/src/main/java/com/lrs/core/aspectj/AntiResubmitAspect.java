package com.lrs.core.aspectj;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.lrs.common.annotation.AntiResubmit;
import com.lrs.common.constant.RedisKey;
import com.lrs.common.exception.ServiceException;
import com.lrs.common.utils.RemoteIpUtil;
import com.lrs.core.base.BaseController;
import com.lrs.core.satoken.StpKit;
import com.lrs.core.system.entity.SysUser;
import com.lrs.core.util.AopUtil;
import com.lrs.core.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 防重复提交切面
 */
@Slf4j
@Aspect
@Component
public class AntiResubmitAspect {

    private static final String LOCK_KEY_PREFIX = RedisKey.REPEAT_SUBMIT_KEY;
    private static final String SPEL_PARSER = "#";

    /**
     * 环绕通知
     */
    @Around(value = "@annotation(antiResubmit)")
    public Object around(ProceedingJoinPoint joinPoint, AntiResubmit antiResubmit) throws Throwable {
        // 生成锁的key
        String lockKey = generateLockKey(joinPoint, antiResubmit);
        String requestId = IdUtil.simpleUUID();
        // 尝试获取锁
        Boolean success = RedisUtil.tryLock(lockKey, requestId, antiResubmit.lockTime());
        if (Boolean.TRUE.equals(success)) {
            try {
                // 获取锁成功，执行目标方法
                return joinPoint.proceed();
            } finally {
                // 方法执行完成后释放锁（或者等待自动过期）
                // 这里可以选择立即删除，也可以等待自动过期
                RedisUtil.releaseLock(lockKey, requestId);
            }
        } else {
            // 获取锁失败，抛出重复提交异常
            throw new ServiceException(antiResubmit.message());
        }
    }

    /**
     * 生成锁的key - 核心逻辑
     */
    private String generateLockKey(ProceedingJoinPoint joinPoint, AntiResubmit antiResubmit) {
        StringBuilder keyBuilder = new StringBuilder(LOCK_KEY_PREFIX);
        // 基础key（方法签名或自定义key）
        String baseKey = buildBaseKey(joinPoint, antiResubmit);
        keyBuilder.append(baseKey);

        // 处理key生成策略
        String keyByStrategy = buildKeyByStrategy(joinPoint, antiResubmit);
        if (StringUtils.hasText(keyByStrategy)) {
            keyBuilder.append(":").append(keyByStrategy);
        }
        return keyBuilder.toString();
    }

    /**
     * 构建基础key
     */
    private String buildBaseKey(ProceedingJoinPoint joinPoint, AntiResubmit antiResubmit) {
        String key = antiResubmit.key();
        if (StringUtils.hasText(key)) {
            // 处理SpEL表达式
            if (key.contains(SPEL_PARSER)) {
                return AopUtil.parseSpel(key, joinPoint);
            }
            return key;
        }
        // 默认使用类名+方法名
        return buildMethodSignatureKey(joinPoint);
    }

    /**
     * 构建方法签名key
     */
    private String buildMethodSignatureKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getSimpleName() + ":" + method.getName();
    }

    /**
     * 根据策略构建key
     */
    private String buildKeyByStrategy(ProceedingJoinPoint joinPoint, AntiResubmit antiResubmit) {
        StringBuilder keyBuilder = new StringBuilder();

        HttpServletRequest request = BaseController.getRequest();
        // 根据锁类型添加不同标识
        switch (antiResubmit.lockType()) {
            case APP_USER:
                String appUserId = getCurrentUserId();
                if (StringUtils.hasText(appUserId)) {
                    addSeparator(keyBuilder);
                    keyBuilder.append("app_user:").append(appUserId);
                }
                break;
            case SYSTEM_USER:
                SysUser loginSysUser = BaseController.getLoginSysUser();
                String userId = String.valueOf(loginSysUser.getId());
                if (StringUtils.hasText(userId)) {
                    addSeparator(keyBuilder);
                    keyBuilder.append("system_user:").append(userId);
                }
                break;
            case IP:
                String clientIp = RemoteIpUtil.getRemoteIpSafely(request);
                if (StringUtils.hasText(clientIp)) {
                    addSeparator(keyBuilder);
                    keyBuilder.append("ip:").append(clientIp);
                }
                break;
            case SESSION:
                String sessionId = StpUtil.getSession().getId();
                if (StringUtils.hasText(sessionId)) {
                    addSeparator(keyBuilder);
                    keyBuilder.append("session:").append(sessionId);
                }
                break;
            case PARAM:
                // 参数级别的锁，使用所有参数hash
                String paramHash = generateParamHash(joinPoint);
                if (StringUtils.hasText(paramHash)) {
                    addSeparator(keyBuilder);
                    keyBuilder.append("param:").append(paramHash);
                }
                break;
        }

        return keyBuilder.toString();
    }

    // 工具方法
    private String getCurrentUserId() {
        try {
            return StpKit.USER.getTokenValue();
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private String generateParamHash(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return "no_params";
            }
            String paramStr = Arrays.stream(args)
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.joining("|"));
            return DigestUtils.md5DigestAsHex(paramStr.getBytes());
        } catch (Exception e) {
            log.warn("生成参数hash失败", e);
            return "error";
        }
    }

    private void addSeparator(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append(":");
        }
    }
}
