package com.leif.controller;

import com.leif.service.WeChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/wechat")
@Slf4j
public class WeChatController {

    @Autowired
    private WeChatService weChatService;

    @RequestMapping("callback")
    public String postCallback(HttpServletRequest request) {

        /**
         * 若确认此次GET请求来自微信服务器，请原样返回echostr参数内容，则接入生效，成为开发者成功，否则接入失败。加密/校验流程如下：
         *
         * 1）将token、timestamp、nonce三个参数进行字典序排序
         * 2）将三个参数字符串拼接成一个字符串进行sha1加密
         * 3）开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
         */
        try {
            String signature = request.getParameter("signature");
            String nonce = request.getParameter("nonce");
            String timestamp = request.getParameter("timestamp");

            String echostr = request.getParameter("echostr");
            String checkResult = weChatService.checkSignature(signature, nonce, timestamp);
            if(StringUtils.isNoneEmpty(checkResult)) {
                return "error";
            }

            if(StringUtils.isNoneEmpty(echostr)) {
                return echostr;
            }

            //获取加密类型 如果是密文，则为aes
            String encryptType = request.getParameter("encrypt_type");
            //获取加密密匙
            String msgSignature = request.getParameter("msg_signature");
            log.info("encryptType:{}, msgSignature:{}",encryptType, msgSignature);
            //获取消息内容
            String message = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

            String result = null;
            if (StringUtils.equals("aes", encryptType)) {
                //如果是密文
                result = weChatService.callbackEvent(message, nonce, timestamp, msgSignature);

            } else {
                //如果是明文
                result = weChatService.callbackEvent(message);
            }
            return result;


        } catch (IOException e) {
            e.printStackTrace();
            return "success";
        }
    }
}
