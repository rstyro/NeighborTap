package com.lrs.core.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Accessors(chain = true)
@Data
public class MiniUserVo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * token
     */
    private String token;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像图片地址
     */
    private String avatarUrl;

    /**
     * 用户的性别，值为1时是男性，值为0时是女性，值2为时是未知
     */
    private Short sex;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 城市
     */
    private String city;

    /**
     * 省
     */
    private String province;

    /**
     * 国家
     */
    private String country;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 是否 正常，1 -- 正常，2-- 锁定
     */
    private Integer status;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime birthday;
}
