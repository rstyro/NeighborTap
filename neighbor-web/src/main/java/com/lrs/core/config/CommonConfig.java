package com.lrs.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@ConfigurationProperties(prefix = "admin.common")
@Configuration
@Data
public class CommonConfig {
    /**
     * index页面的名称配置
     */
    private String systemName;

    private UploadConfig upload;
    private SecurityConfig security;
    private UserConfig user;

    @Data
    public static class UploadConfig {
        /**
         * 上传的根目录
         */
        private String root;
        /**
         * 上传文件地址前缀
         */
        private String pre;

        public String getRoot() {
            // 如果没有写入具体路径，就使用相对于项目根目录
            return "upload".equals(root)?System.getProperty("user.dir")+"/"+root:root;
        }

    }

    @Data
    public static class SecurityConfig {
        /**
         * 免登录的路径
         */
        private List<String> excludes;

    }

    @Data
    public static class UserConfig {
        /**
         * 密码最大错误次数
         */
        private int maxRetryCount;
        /**
         * 密码锁定时间,单位分钟
         */
        private int lockTime;
    }
}
