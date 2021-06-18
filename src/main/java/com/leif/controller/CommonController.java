package com.leif.controller;

import com.leif.model.dto.SendVerifyCodeDto;
import com.leif.service.SendSmsService;
import com.leif.util.result.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private SendSmsService sendSmsService;

    /**
     * 发送验证码
     * @param sendVerifyCodeDto
     * @return
     */
    @PostMapping("/verify_code")
    public ApiResult sendVerifyCode(@RequestBody SendVerifyCodeDto sendVerifyCodeDto) {
        sendSmsService.sendVerifyCode(sendVerifyCodeDto);
        return ApiResult.SUCCESS();
    }

}
