package com.leif.service;

import com.leif.model.dto.request.SendVerifyCodeDto;

public interface SendSmsService {

    /**
     * 发送短信验证码
     * @param sendVerifyCodeDto
     */
    void sendVerifyCode(SendVerifyCodeDto sendVerifyCodeDto);
}
