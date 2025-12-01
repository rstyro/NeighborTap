package com.lrs.core.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lrs.core.admin.entity.AppUser;
import com.lrs.core.system.dto.BaseDto;

import java.util.List;


/**
 * <p>
 * 小程序-用户基本信息表 服务类
 * </p>
 *
 * @author rstyro
 * @since 2025年11月28日
 */
public interface IAppUserService extends IService<AppUser> {

    Page<AppUser> getPage(Page page, BaseDto dto);

    boolean add(AppUser item);

    boolean edit(AppUser item);

    boolean del(Long id);

    boolean batchDel(List<Long> ids);

    AppUser getUserByOpenId(String openId);
}
