package com.lrs.core.aspectj;

import cn.dev33.satoken.SaManager;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.lrs.common.annotation.RepeatSubmit;
import com.lrs.common.constant.Const;
import com.lrs.common.constant.RedisKey;
import com.lrs.common.exception.ServiceException;
import com.lrs.common.utils.GsonUtil;
import com.lrs.common.utils.RedisSimulation;
import com.lrs.common.vo.R;
import com.lrs.core.base.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 防止重复提交切面
 *
 * @author lrs
 */
@Aspect
@Component
public class RepeatSubmitAspect {

    private static final int MIN_INTERVAL = 1000;
    private static final String PARAM_JOINER = " ";
    private final RedisSimulation redisSimulation;

    /**
     * 使用ThreadLocal存储当前请求的重复提交键，确保线程安全
     */
    private static final ThreadLocal<String> CURRENT_KEY = new ThreadLocal<>();

    public RepeatSubmitAspect() {
        this.redisSimulation = SpringUtil.getBean(RedisSimulation.class);
    }

    @Before("@annotation(repeatSubmit)")
    public void doBefore(JoinPoint point, RepeatSubmit repeatSubmit) {
        // 验证时间间隔
        long interval = repeatSubmit.timeUnit().toMillis(repeatSubmit.interval());
        if (interval < MIN_INTERVAL) {
            throw new ServiceException("重复提交间隔时间不能小于1秒");
        }

        HttpServletRequest request = BaseController.getRequest();
        String requestParams = buildRequestParams(point.getArgs());
        String cacheKey = buildCacheKey(request, requestParams);

        // 尝试获取锁，如果已存在则抛出异常
        if (!redisSimulation.setObjectIfAbsent(cacheKey, "", interval)) {
            throw new ServiceException(repeatSubmit.message());
        }

        CURRENT_KEY.set(cacheKey);
    }

    @AfterReturning(pointcut = "@annotation(repeatSubmit)", returning = "result")
    public void doAfterReturning(RepeatSubmit repeatSubmit, Object result) {
        try {
            if (result instanceof R) {
                R<?> r = (R<?>) result;
                // 仅当业务失败时删除键，允许重新提交
                if (r.getCode() != R.SUCCESS) {
                    removeCurrentKey();
                }
            }
        } finally {
            cleanup();
        }
    }

    @AfterThrowing(value = "@annotation(repeatSubmit)", throwing = "e")
    public void doAfterThrowing(RepeatSubmit repeatSubmit,Exception e) {
        removeCurrentKey();
        cleanup();
    }

    /**
     * 构建请求参数字符串
     */
    private String buildRequestParams(Object[] params) {
        if (ArrayUtil.isEmpty(params)) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(PARAM_JOINER);
        for (Object param : params) {
            if (shouldIncludeParam(param)) {
                joiner.add(GsonUtil.toJson(param));
            }
        }
        return joiner.toString();
    }

    /**
     * 构建Redis缓存键
     */
    private String buildCacheKey(HttpServletRequest request, String params) {
        String token = StrUtil.trimToEmpty(request.getHeader(SaManager.getConfig().getTokenName()));
        String requestUri = request.getRequestURI();
        String uniqueKey = SecureUtil.md5(token + ":" + params);

        return RedisKey.REPEAT_SUBMIT_KEY + requestUri + uniqueKey;
    }

    /**
     * 判断参数是否应该包含在重复提交校验中
     */
    private boolean shouldIncludeParam(Object param) {
        if (ObjectUtil.isNull(param)) {
            return false;
        }
        Class<?> clazz = param.getClass();
        if (clazz.isArray()) {
            return !clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (param instanceof Collection) {
            return ((Collection<?>) param).stream().noneMatch(this::isFileOrRequest);
        } else if (param instanceof Map) {
            return ((Map<?, ?>) param).values().stream().noneMatch(this::isFileOrRequest);
        }
        return !isFileOrRequest(param);
    }

    /**
     * 判断是否为文件或Servlet对象
     */
    private boolean isFileOrRequest(Object obj) {
        return obj instanceof MultipartFile
                || obj instanceof HttpServletRequest
                || obj instanceof HttpServletResponse
                || obj instanceof BindingResult;
    }

    /**
     * 移除当前线程的Redis键
     */
    private void removeCurrentKey() {
        String key = CURRENT_KEY.get();
        if (key != null) {
            redisSimulation.del(key);
        }
    }

    /**
     * 清理ThreadLocal
     */
    private void cleanup() {
        CURRENT_KEY.remove();
    }
}