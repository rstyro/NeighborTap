package com.lrs.core.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lrs.core.admin.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 小程序-用户基本信息表 Mapper 接口
 * </p>
 *
 * @author rstyro
 * @since 2025-11-28
 */
@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {

    AppUser getUserByOpenId(@Param("openId") String openId);
}
