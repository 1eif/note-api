package com.leif.model.dto.request;

import lombok.Data;

/**
 * 用户信息设置Dto
 */
@Data
public class UserSettingDto {

    private String userId;
    private String nickName;
    private String oldPassword;
    private String newPassword;
}
