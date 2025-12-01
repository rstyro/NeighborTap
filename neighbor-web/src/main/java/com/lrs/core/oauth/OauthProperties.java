package com.lrs.core.oauth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;


@Data
@ConfigurationProperties(prefix = "oauth")
@Configuration
public class OauthProperties {

    /**
     * 实现就添加枚举
     */
    private Map<OauthSource, OAuthConfig> clients;


    /**
     * oauth 相关配置，可扩展
     */
    @Data
    public static class OAuthConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        /**
         * 是否获取unionId
         */
        private boolean unionId;
    }

}
