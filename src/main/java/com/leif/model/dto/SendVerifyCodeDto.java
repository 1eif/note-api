package com.leif.model.dto;

import lombok.Data;

/**
 * 发送验证码Dto
 */
@Data
public class SendVerifyCodeDto {

    /**
     * 发送短信验证码场景值
     * TODO
     */



    private String phone;
    private int sceneCode;
    private String device;
}
