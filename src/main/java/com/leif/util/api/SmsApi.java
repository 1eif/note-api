package com.leif.util.api;

import com.leif.config.SmsConfigProperty;
import com.leif.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Slf4j
@Component
//泛指各种组件，就是说当我们的类不属于各种归类的时候（不属于@Controller、@Services等的时候），我们就可以使用@Component来标注这个类。
public class SmsApi {

    @Autowired
    private SmsConfigProperty smsConfigProperty;

    /**
     * 发送验证码
     */
//    public void sendVerifyCode(String phone, String verifyCode) {
//        String message = "【Leif】网址注册码为" + verifyCode + ",验证码10分钟内有效。";
//        sendSms(phone, message);
//    }

    public void sendSms(String phone, String message) {
        String encodeMessage = encodeString(message);
        String url = "https://api.smsbao.com/sms?u=" + smsConfigProperty.getUsername() + "&p=" + smsConfigProperty.getPassword()
                + "&m=" + phone + "&c=" + encodeMessage;
        String result = HttpUtil.sendGetRequest(url);

        if (result.equals("0")) {
            log.info("{} 发送短信： {} 成功", phone, message);
        } else {
            log.info("{} 发送短信： {} 失败，错误码{}", phone, message, result);
        }
    }

    public String encodeString(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
