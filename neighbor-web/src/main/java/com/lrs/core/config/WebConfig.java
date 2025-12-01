package com.lrs.core.config;


import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.lrs.core.intercept.ContextInterceptor;
import com.lrs.core.intercept.LoginIntercept;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 接口拦截
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private CommonConfig commonConfig;

    @Bean
    public LoginIntercept loginIntercept() {
        return new LoginIntercept();
    }

    @Bean
    public ContextInterceptor contextInterceptor(){
        return new ContextInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        CommonConfig.SecurityConfig security = commonConfig.getSecurity();
        List<String> excludes = Objects.nonNull(security)?commonConfig.getSecurity().getExcludes():new ArrayList<>();
        registry.addInterceptor(loginIntercept())
                .addPathPatterns("/**")
                .excludePathPatterns(excludes);
        registry.addInterceptor(contextInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(excludes);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //  映射favicon.ico，解决No endpoint GET /favicon.ico
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/","classpath:/resources/");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
