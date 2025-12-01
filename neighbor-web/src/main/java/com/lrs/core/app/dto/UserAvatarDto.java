package com.lrs.core.app.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Data
@Accessors(chain = true)
public class UserAvatarDto {

    // 头像地址
    private String avatarUrl;

    // 头像文件
    private MultipartFile avatarFile;
}
