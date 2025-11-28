package com.lrs.core.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 小程序-用户基本信息表
 * </p>
 *
 * @author rstyro
 * @since 2025-11-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("app_user")
public class AppUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 昵称
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 微信头像图片地址
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 用户的性别，值为1时是男性，值为0时是女性，值为2时是未知
     */
    @TableField("sex")
    private Short sex;

    /**
     * 个性签名
     */
    @TableField("signature")
    private String signature;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 省
     */
    @TableField("province")
    private String province;

    /**
     * 国家
     */
    @TableField("country")
    private String country;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号码
     */
    @TableField("phone")
    private String phone;

    /**
     * 是否 正常，1 -- 正常，2-- 锁定
     */
    @TableField("status")
    private Integer status;

    /**
     * 生日
     */
    @TableField("birthday")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime birthday;

    /**
     * 最后登录IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 是否已删除
     */
    @TableField("is_del")
    private Integer isDel;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}
