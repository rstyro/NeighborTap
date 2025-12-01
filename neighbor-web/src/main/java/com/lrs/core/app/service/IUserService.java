package com.lrs.core.app.service;

import com.lrs.core.app.dto.MiniLoginDto;
import com.lrs.core.app.dto.UserAvatarDto;
import com.lrs.core.app.dto.UserInfoDto;
import com.lrs.core.app.vo.MiniUserVo;

public interface IUserService {

    /**
     * 小程序登录
     * @param dto
     * @return
     */
    MiniUserVo appletLogin(MiniLoginDto dto);

    /**
     * 更新用户基本信息
     */
    boolean updateUserInfo(UserInfoDto dto);

    /**
     * 更新头像
     */
    String updateUserAvatar(UserAvatarDto dto);
}
