package com.leif.model.dto.request;

import lombok.Data;

/**
 * 忘记密码：设置新密码Dto
 */
@Data
public class ForgetPasswordSetNewPasswordDto {

    private String token;
    private String password;
}
