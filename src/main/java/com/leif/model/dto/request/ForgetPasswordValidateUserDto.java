package com.leif.model.dto.request;

import lombok.Data;

/**
 * 忘记密码：验证用户有效性Dto
 */
@Data
public class ForgetPasswordValidateUserDto {

    private String phone;
    private String tokenID;
    private String verifyCode;
}
