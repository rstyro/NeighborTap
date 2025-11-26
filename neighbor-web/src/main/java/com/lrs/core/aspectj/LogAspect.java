package com.lrs.core.aspectj;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.lrs.common.annotation.OperateLog;
import com.lrs.common.constant.SystemConst;
import com.lrs.common.utils.GsonUtil;
import com.lrs.core.base.BaseController;
import com.lrs.core.system.entity.SysUser;
import com.lrs.core.system.event.OperLogEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 操作日志记录切面
 * 复制自ruoyi修改
 * @author ruoyi
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * 排除敏感属性字段
     */
    private static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};

    /**
     * 计算操作消耗时间 - 使用TransmittableThreadLocal解决线程池上下文传递问题
     */
    private static final ThreadLocal<StopWatch> TIME_THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 日志参数最大长度限制
     */
    private static final int PARAM_MAX_LENGTH = 2000;
    private static final int ERROR_MSG_MAX_LENGTH = 2000;
    private static final int URL_MAX_LENGTH = 255;

    @Before(value = "@annotation(controllerLog)")
    public void boBefore(JoinPoint joinPoint, OperateLog controllerLog) {
        StopWatch stopWatch = new StopWatch();
        TIME_THREAD_LOCAL.set(stopWatch);
        stopWatch.start();
    }

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, OperateLog controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, OperateLog controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    /**
     * 处理日志记录 - 主逻辑方法
     */
    private void handleLog(final JoinPoint joinPoint, OperateLog controllerLog,
                           final Exception e, Object jsonResult) {
        StopWatch stopWatch = TIME_THREAD_LOCAL.get();
        if (stopWatch == null || !stopWatch.isRunning()) {
            log.warn("StopWatch未初始化或未运行，跳过日志记录");
            return;
        }

        try {
            OperLogEvent operLog = buildOperLog(joinPoint, controllerLog, e, jsonResult, stopWatch);
            SpringUtil.getApplicationContext().publishEvent(operLog);
        } catch (Exception exp) {
            log.error("操作日志记录异常, 方法: {}.{}, 异常信息: {}",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    exp.getMessage(), exp);
        } finally {
            cleanupThreadLocal();
        }
    }

    /**
     * 构建操作日志对象
     */
    private OperLogEvent buildOperLog(JoinPoint joinPoint, OperateLog controllerLog,
                                      Exception e, Object jsonResult, StopWatch stopWatch) {
        OperLogEvent operLog = new OperLogEvent();

        // 设置基础信息
        setupBasicInfo(operLog, e);

        // 设置用户信息
        setupUserInfo(operLog);

        // 设置方法信息
        setupMethodInfo(operLog, joinPoint);

        // 处理注解配置
        processAnnotationConfig(joinPoint, controllerLog, operLog, jsonResult);

        // 设置性能指标
        setupPerformanceMetrics(operLog, stopWatch);

        return operLog;
    }

    /**
     * 设置基础信息
     */
    private void setupBasicInfo(OperLogEvent operLog, Exception e) {
        operLog.setStatus(SystemConst.OperateLogStatus.SUCCESS);
        operLog.setOperIp(NetUtil.getLocalhostStr());
        operLog.setOperTime(LocalDateTime.now());

        if (e != null) {
            operLog.setStatus(SystemConst.OperateLogStatus.FAIL);
            operLog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, ERROR_MSG_MAX_LENGTH));
        }
    }

    /**
     * 设置用户信息
     */
    private void setupUserInfo(OperLogEvent operLog) {
        try {
            SysUser loginSysUser = BaseController.getLoginSysUser();
            if (loginSysUser != null) {
                operLog.setOperName(loginSysUser.getUsername());
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
        }
    }

    /**
     * 设置方法信息
     */
    private void setupMethodInfo(OperLogEvent operLog, JoinPoint joinPoint) {
        HttpServletRequest request = BaseController.getRequest();
        if (request != null) {
            operLog.setOperUrl(StrUtil.sub(request.getRequestURI(), 0, URL_MAX_LENGTH));
            operLog.setRequestMethod(request.getMethod());
        }

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        operLog.setMethod(className + "." + methodName + "()");
    }

    /**
     * 处理注解配置
     */
    private void processAnnotationConfig(JoinPoint joinPoint, OperateLog logAnnotation,
                                         OperLogEvent operLog, Object jsonResult) {
        operLog.setBusinessType(logAnnotation.businessType().ordinal());
        operLog.setTitle(logAnnotation.title());
        operLog.setOperatorType(logAnnotation.operatorType().ordinal());

        if (logAnnotation.isSaveRequestData()) {
            setRequestValue(joinPoint, operLog, logAnnotation.excludeParamNames());
        }

        if (logAnnotation.isSaveResponseData() && ObjectUtil.isNotNull(jsonResult)) {
            operLog.setJsonResult(StrUtil.sub(GsonUtil.toJson(jsonResult), 0, PARAM_MAX_LENGTH));
        }
    }

    /**
     * 设置性能指标
     */
    private void setupPerformanceMetrics(OperLogEvent operLog, StopWatch stopWatch) {
        stopWatch.stop();
        operLog.setCostTime(stopWatch.getTotalTimeMillis());
    }

    /**
     * 清理线程本地变量
     */
    private void cleanupThreadLocal() {
        try {
            TIME_THREAD_LOCAL.remove();
        } catch (Exception e) {
            log.warn("清理线程本地变量异常: {}", e.getMessage());
        }
    }

    /**
     * 获取请求参数值
     */
    private void setRequestValue(JoinPoint joinPoint, OperLogEvent operLog, String[] excludeParamNames) {
        try {
            HttpServletRequest request = BaseController.getRequest();
            if (request == null) {
                return;
            }

            Map<String, String> paramsMap = BaseController.getParamMap(request);
            String requestMethod = operLog.getRequestMethod();

            String params = shouldUseArgsArray(paramsMap, requestMethod)
                    ? buildParamsFromArgs(joinPoint.getArgs(), excludeParamNames)
                    : buildParamsFromMap(paramsMap, excludeParamNames);

            operLog.setOperParam(StrUtil.sub(params, 0, PARAM_MAX_LENGTH));
        } catch (Exception e) {
            log.warn("设置请求参数值失败: {}", e.getMessage());
        }
    }

    /**
     * 判断是否应该使用方法参数构建参数字符串
     */
    private boolean shouldUseArgsArray(Map<String, String> paramsMap, String requestMethod) {
        return MapUtil.isEmpty(paramsMap) &&
                (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod));
    }

    /**
     * 从方法参数构建参数字符串
     */
    private String buildParamsFromArgs(Object[] paramsArray, String[] excludeParamNames) {
        if (ArrayUtil.isEmpty(paramsArray)) {
            return "";
        }
        StringJoiner params = new StringJoiner(" ");
        for (Object param : paramsArray) {
            if (ObjectUtil.isNotNull(param) && !isFilterObject(param)) {
                params.add(processSingleParam(param, excludeParamNames));
            }
        }
        return params.toString();
    }

    /**
     * 处理单个参数
     */
    private String processSingleParam(Object param, String[] excludeParamNames) {
        try {
            String jsonStr = GsonUtil.toJson(param);
            Dict dict = GsonUtil.fromJson(jsonStr, Dict.class);

            if (MapUtil.isNotEmpty(dict)) {
                MapUtil.removeAny(dict, EXCLUDE_PROPERTIES);
                MapUtil.removeAny(dict, excludeParamNames);
                return GsonUtil.toJson(dict);
            }
            return jsonStr;
        } catch (Exception e) {
            log.warn("参数处理异常: {}", e.getMessage());
            return "参数序列化失败";
        }
    }

    /**
     * 从参数Map构建参数字符串
     */
    private String buildParamsFromMap(Map<String, String> paramsMap, String[] excludeParamNames) {
        if (MapUtil.isEmpty(paramsMap)) {
            return "";
        }

        Map<String, String> filteredMap = new HashMap<>(paramsMap);
        MapUtil.removeAny(filteredMap, EXCLUDE_PROPERTIES);
        MapUtil.removeAny(filteredMap, excludeParamNames);

        return GsonUtil.toJson(filteredMap);
    }

    /**
     * 判断是否需要过滤的对象
     */
    public boolean isFilterObject(final Object param) {
        if (param == null) {
            return true;
        }

        Class<?> clazz = param.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (param instanceof Collection) {
            return ((Collection<?>) param).stream().anyMatch(this::isFileOrRequest);
        } else if (param instanceof Map) {
            return ((Map<?, ?>) param).values().stream().anyMatch(this::isFileOrRequest);
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
}