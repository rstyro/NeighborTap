package com.lrs.core.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrs.common.constant.Const;
import com.lrs.common.enums.ApiResultEnum;
import com.lrs.common.exception.ServiceException;
import com.lrs.common.vo.SecurityContextHolder;
import com.lrs.core.admin.entity.AppUser;
import com.lrs.core.admin.entity.AppUserOauth;
import com.lrs.core.admin.service.IAppUserOauthService;
import com.lrs.core.admin.service.IAppUserService;
import com.lrs.core.app.dto.MiniLoginDto;
import com.lrs.core.app.dto.UserAvatarDto;
import com.lrs.core.app.dto.UserInfoDto;
import com.lrs.core.app.service.IUserService;
import com.lrs.core.app.vo.MiniUserVo;
import com.lrs.core.config.CommonConfig;
import com.lrs.core.oauth.OauthSource;
import com.lrs.core.oauth.OauthUtils;
import com.lrs.core.satoken.StpKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {

    private final IAppUserService appUserService;
    private final IAppUserOauthService appUserOauthService;
    private final CommonConfig commonConfig;

    @Override
    public MiniUserVo appletLogin(MiniLoginDto dto) {
        AuthRequest authRequest = OauthUtils.createAuthRequest(OauthSource.WECHAT_MINI_PROGRAM);
        AuthToken accessToken = authRequest.getAccessToken(AuthCallback.builder().code(dto.getCode()).build());
        AppUser user = appUserService.getUserByOpenId(accessToken.getOpenId());
        if (null == user) {
            // 初始化
            user = new AppUser();
            BeanUtil.copyProperties(dto, user);
            appUserService.save(user);
            // 绑定第三方
            bindMiniProgramOauth(user.getUserId(), accessToken);
        }
        StpKit.APP.login(user.getUserId());
        return reloadUserInfo(user);
    }

    @Override
    public boolean updateUserInfo(UserInfoDto dto) {
        Long userId = SecurityContextHolder.getUserId();
        AppUser user = appUserService.getById(userId);
        if (ObjectUtils.isEmpty(user)) {
            throw new ServiceException(ApiResultEnum.APP_USER_NOT_FOUND);
        }
        // 检查昵称唯一性
        if (!ObjectUtils.isEmpty(dto.getNickName())) {
            checkNicknameUnique(dto.getNickName(), userId);
        }
        // 更新信息
        AppUser updateUser = new AppUser();
        BeanUtil.copyProperties(dto, updateUser);
        updateUser.setUserId(userId);
        boolean sucUpdate = appUserService.updateById(updateUser);
        if (sucUpdate) {
            // 更新成功刷新用户 session 信息
            reloadUserInfo(user);
        }
        return sucUpdate;
    }

    @Override
    public String updateUserAvatar(UserAvatarDto dto) {
        String avatarUrl = dto.getAvatarUrl();
        MultipartFile avatarFile = dto.getAvatarFile();

        // 参数校验
        if (ObjectUtils.isEmpty(avatarFile) && ObjectUtils.isEmpty(avatarUrl)) {
            throw new ServiceException(ApiResultEnum.ERROR_INVALID_PARAM, "头像文件或头像链接不能为空");
        }

        String folder = "/avatar/";
        String fileName = IdUtil.fastSimpleUUID() + ".png";
        // 获取文件名
        File dest = new File(commonConfig.getUpload().getRoot() + folder + fileName);
        try {
            // 确保目录存在
            dest.getParentFile().mkdirs();

            if (!ObjectUtils.isEmpty(avatarFile)) {
                avatarFile.transferTo(dest);
            } else if (!ObjectUtils.isEmpty(avatarUrl)) {
                long size = HttpUtil.downloadFile(avatarUrl, dest);
                log.info("上传头像大小={},avatarUrl={}", size, avatarUrl);
            }
            // 更新用户头像
            Long userId = SecurityContextHolder.getUserId();
            String newAvatarUrl = "/show" + folder + fileName;
            appUserService.updateById(new AppUser().setUserId(userId).setAvatarUrl(newAvatarUrl));
            return newAvatarUrl;
        } catch (IOException e) {
            log.error("上传头像失败，err={}", e.getMessage(), e);
            throw new ServiceException(ApiResultEnum.ERROR_IO);
        }
    }

    private void checkNicknameUnique(String nickname, Long userId) {
        boolean exists = appUserService.exists(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getNickName, nickname)
                .ne(AppUser::getUserId, userId));
        if (exists) {
            throw new ServiceException(ApiResultEnum.APP_USER_NICK_NAME_EXIST);
        }
    }

    /**
     * 绑定小程序
     */
    private void bindMiniProgramOauth(Long userId, AuthToken accessToken) {
        long count = appUserOauthService.count(new LambdaQueryWrapper<AppUserOauth>()
                .eq(AppUserOauth::getUserId, userId)
                .eq(AppUserOauth::getOpenId, accessToken.getOpenId()));
        if (count > 0) {
            return;
        }
        AppUserOauth oauth = new AppUserOauth();
        oauth.setUserId(userId);
        oauth.setOpenId(accessToken.getOpenId());
        oauth.setUnionId(accessToken.getUnionId());
        appUserOauthService.save(oauth);
    }

    private MiniUserVo reloadUserInfo(AppUser user) {
        if (ObjectUtils.isEmpty(user)) return null;
        MiniUserVo vo = new MiniUserVo();
        BeanUtil.copyProperties(user, vo);
        vo.setToken(StpKit.APP.getTokenValue());
        StpKit.APP.getSession().set(Const.SessionKey.APP_SESSION_USER, vo);
        return vo;
    }
}
