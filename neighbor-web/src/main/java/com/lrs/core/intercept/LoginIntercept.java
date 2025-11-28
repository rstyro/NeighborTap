package com.lrs.core.intercept;

import cn.dev33.satoken.annotation.SaIgnore;
import com.lrs.common.enums.ApiResultEnum;
import com.lrs.common.exception.ServiceException;
import com.lrs.core.satoken.StpKit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截，未登录重定向到登陆页面
 */
@Slf4j
public class LoginIntercept implements HandlerInterceptor {

    // 单例模式，避免每次调用都新建实例
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("LoginIntercept-ServletPath={}", request.getServletPath());
        }
        String servletPath = request.getServletPath();
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //  如果方法有@SaIgnore 也直接返回true
        if (hasSaIgnoreAnnotation(handler)) {
            if (log.isDebugEnabled()) {
                log.debug("LoginIntercept-忽略认证路径，url={}", servletPath);
            }
            return true;
        }

        // 判断是否是APP用户体系的
        if(MATCHER.match("/app/**", servletPath)){
            // 若user模块且未登录，则提示用户
            if (StpKit.APP.isLogin()) {
                return true;
            }
            log.debug("LoginIntercept-用户未登录拦截，url={}", servletPath);
            throw new ServiceException(ApiResultEnum.APP_USER_NO_LOGIN_OR_EXPIRED);
        }

        // 若admin模块且未登录，则重定向到登录页面
        if (!StpKit.ADMIN.isLogin()) {
            log.debug("LoginIntercept-admin路径未登录拦截，url={}", servletPath);
            log.info("{} 重定向登录页",request.getServletPath());
            response.sendRedirect(request.getContextPath() + "/toLogin");
            return false;
        }

        return true;
    }

    /**
     * 检查处理器是否有@SaIgnore注解（类级别或方法级别）
     */
    private boolean hasSaIgnoreAnnotation(Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 检查方法上的注解
        SaIgnore methodAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), SaIgnore.class);
        if (methodAnnotation != null) {
            return true;
        }
        // 检查类上的注解
        SaIgnore classAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), SaIgnore.class);
        return classAnnotation != null;
    }
}
