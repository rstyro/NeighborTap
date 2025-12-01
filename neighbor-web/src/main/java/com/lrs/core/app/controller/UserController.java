package com.lrs.core.app.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.lrs.common.annotation.AntiResubmit;
import com.lrs.common.annotation.OperateLog;
import com.lrs.common.constant.Const;
import com.lrs.common.enums.OperatorType;
import com.lrs.common.vo.R;
import com.lrs.core.app.dto.MiniLoginDto;
import com.lrs.core.app.dto.UserAvatarDto;
import com.lrs.core.app.dto.UserInfoDto;
import com.lrs.core.app.service.IUserService;
import com.lrs.core.satoken.StpKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * app用户相关
 */
@Slf4j
@RequestMapping("/app/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 小程序登录
     */
    @OperateLog(title = "小程序登录",operatorType = OperatorType.MOBILE)
    @AntiResubmit
    @SaIgnore
    @PostMapping("/appletLogin")
    public R login(@RequestBody MiniLoginDto dto){
        return R.ok(userService.appletLogin(dto));
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/getUserInfo")
    public R getUserInfo(){
        return R.ok(StpKit.APP.getSession().get(Const.SessionKey.APP_SESSION_USER));
    }

    /**
     * 更新用户信息
     * @param dto 参数
     * @return boolean
     */
    @OperateLog(title = "更新用户信息")
    @PostMapping(value = "/updateUserInfo")
    public R<Boolean> updateUserInfo(@RequestBody UserInfoDto dto){
        return R.ok(userService.updateUserInfo(dto));
    }

    /**
     * APP是否登录
     */
    @SaIgnore
    @GetMapping("/isLogin")
    public R isLogin(){
        return R.ok(StpKit.APP.isLogin());
    }

    /**
     * 上传头像
     */
    @OperateLog(title = "更新头像")
    @PostMapping(value = "/updateUserAvatar")
    public R<String> userAvatar(UserAvatarDto dto){
        return R.ok(userService.updateUserAvatar(dto));
    }


    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R logout(){
        StpKit.APP.logout();
        return R.ok();
    }



}
