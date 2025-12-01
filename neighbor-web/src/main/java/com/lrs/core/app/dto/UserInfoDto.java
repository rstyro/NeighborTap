package com.lrs.core.app.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UserInfoDto {

    // 昵称
    private String nickName;
    private String avatarUrl;
    private Short sex;
    private String signature;

    private String city;
    private String province;
    private String country;

    private String email;

    private String phone;

    private LocalDateTime birthday;

}
