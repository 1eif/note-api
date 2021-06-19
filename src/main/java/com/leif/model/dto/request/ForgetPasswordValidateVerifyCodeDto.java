package com.leif.model.dto.request;

import lombok.Data;

/**
 * 忘记密码：验证验证码有效性Dto
 */
@Data
public class ForgetPasswordValidateVerifyCodeDto {
    private String token;
    private String verifyCode;

}
