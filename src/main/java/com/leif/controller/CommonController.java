package com.leif.controller;

import com.leif.model.dto.SendVerifyCodeDto;
import com.leif.service.SendSmsService;
import com.leif.util.result.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonController {

    @Autowired
    private SendSmsService sendSmsService;

    /**
     *发送验证码
     * @return
     */
    @PostMapping("common/verify_code")
    public ApiResult sendVerifyCode(@RequestBody SendVerifyCodeDto sendVerifyCodeDto) {
        sendSmsService.sendVerifyCode(sendVerifyCodeDto);
        return ApiResult.SUCCESS();
    }

}
