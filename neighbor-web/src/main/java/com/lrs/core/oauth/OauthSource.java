package com.lrs.core.oauth;

import lombok.Getter;

@Getter
public enum OauthSource {
    WECHAT_MINI_PROGRAM("wechatmini","微信小程序"),
    QQ("qq","QQ"),
    WECHAT_OPEN("wechat_open","微信开放平台"),

    ;
    private String source;
    private String description;

    OauthSource(String source, String description) {
        this.source = source;
        this.description = description;
    }
}
