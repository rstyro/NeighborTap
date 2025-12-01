package com.lrs.core.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 小程序-用户第三方登录账号绑定
 * </p>
 *
 * @author rstyro
 * @since 2025-12-01
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("app_user_oauth")
public class AppUserOauth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * openId 开放id,应用唯一标识
     */
    @TableField("open_id")
    private String openId;

    /**
     * unionId 开放平台账号下的所有应用之间共享
     */
    @TableField("union_id")
    private String unionId;

    /**
     * 来源
     */
    @TableField("source")
    private String source;

    /**
     * 昵称
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 头像
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 是否删除
     */
    @TableField("is_del")
    private Integer isDel;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
