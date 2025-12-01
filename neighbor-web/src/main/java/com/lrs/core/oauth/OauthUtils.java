package com.lrs.core.oauth;

import cn.hutool.extra.spring.SpringUtil;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import me.zhyd.oauth.request.AuthWechatMiniProgramRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证授权工具类
 *
 * @author rstyro
 */
public class OauthUtils {

    private static AuthStateRedisCache STATE_CACHE =SpringUtil.getBean(AuthStateRedisCache.class);;

    /**
     * 缓存
     */
    private static final Map<OauthSource, AuthRequest> AUTH_REQUEST_MAP = new ConcurrentHashMap<>();


    public static AuthResponse<AuthUser> loginAuth(OauthSource source, String code, String state) throws AuthException {
        AuthRequest authRequest = AUTH_REQUEST_MAP.computeIfAbsent(source, OauthUtils::createAuthRequest);
        AuthCallback callback = new AuthCallback();
        callback.setCode(code);
        callback.setState(state);
        return authRequest.login(callback);
    }


    public static AuthRequest createAuthRequest(OauthSource source) throws AuthException {
        OauthProperties.OAuthConfig authConfig = getOauthProperties().getClients().get(source);
        if (authConfig == null) {
            throw new AuthException("不支持的第三方登录类型");
        }
        return switch (source) {
            case WECHAT_MINI_PROGRAM -> new AuthWechatMiniProgramRequest(AuthConfig.builder()
                    .clientId(authConfig.getClientId())
                    .clientSecret(authConfig.getClientSecret())
                    // 小程序平台的授权登录不需要回调地址
                    .ignoreCheckRedirectUri(true)
                    .ignoreCheckState(true).build(), STATE_CACHE);
            case QQ -> new AuthQqRequest(buildAuthConfig(authConfig),STATE_CACHE);
            case WECHAT_OPEN -> new AuthWeChatOpenRequest(buildAuthConfig(authConfig), STATE_CACHE);
            default -> throw new AuthException("未获取到有效的Auth配置");
        };
    }

    private static OauthProperties getOauthProperties() {
        return SpringUtil.getBean(OauthProperties.class);
    }

    /**
     * 构建AuthConfig（提取公共逻辑）
     */
    private static AuthConfig buildAuthConfig(OauthProperties.OAuthConfig config) {
        AuthConfig.AuthConfigBuilder builder = AuthConfig.builder()
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .redirectUri(config.getRedirectUri());
        return builder.build();
    }
}

