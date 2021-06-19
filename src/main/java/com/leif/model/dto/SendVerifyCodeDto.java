package com.leif.model.dto;

import lombok.Data;

/**
 * 发送验证码Dto
 */
@Data
public class SendVerifyCodeDto {

    /**
     * 发送短信验证码场景值
     *
     * 1001 注册
     * 1002 忘记密码
     */
    public static final Integer SCENCE_CODE_REGISTER = 1001;
    public static final Integer SCENCE_CODE_FORGET_PASSWORD = 1002;



    private String phone;
    private int sceneCode;
    private String device;
}
