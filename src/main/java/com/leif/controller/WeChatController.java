package com.leif.controller;

import com.leif.exception.ServiceException;
import com.leif.service.WeChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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

        } catch (Exception e) {
            throw new ServiceException("微信服务器接入失败", e);
        }
        return null;
    }
}
