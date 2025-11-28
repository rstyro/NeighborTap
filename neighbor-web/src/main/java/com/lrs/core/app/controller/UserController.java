package com.lrs.core.app.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.lrs.common.annotation.AntiResubmit;
import com.lrs.common.annotation.OperateLog;
import com.lrs.common.vo.R;
import com.lrs.core.app.dto.MiniLoginDto;
import com.lrs.core.satoken.StpKit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * app用户相关
 */
@Slf4j
@RequestMapping("/app/user")
@RestController
public class UserController {

    /**
     * 小程序登录
     */
    @OperateLog(title = "小程序登录")
    @AntiResubmit
    @SaIgnore
    @PostMapping("/appletLogin")
    public R login(@RequestBody MiniLoginDto dto){
        return R.ok();
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
