package com.lrs.core.app.dto;

import lombok.Data;

@Data
public class MiniLoginDto {

    // 微信登录返回的code
    private String code;

    //微信登录返回的信息
    private String nickName;
    // 头像地址
    private String avatarUrl;
    // 性别：0-男，1-女，2-保密
    private Short sex;
    private String country;
    private String province;
    private String city;

    //token验证
    private String Authority;
}
