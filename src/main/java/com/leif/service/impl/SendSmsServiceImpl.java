package com.leif.service.impl;

import com.leif.exception.ServiceException;
import com.leif.model.dto.request.SendSmsDto;
import com.leif.model.dto.request.SendVerifyCodeDto;
import com.leif.service.SendSmsService;
import com.leif.util.SysConst;
import com.leif.util.api.SmsApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SendSmsServiceImpl implements SendSmsService {
    /**
     * 发送短信验证码
     *
     * @param sendVerifyCodeDto
     */
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SmsApi smsApi;

    @Override
    public void sendVerifyCode(SendVerifyCodeDto sendVerifyCodeDto) {
        //60s内是否发过短信
        RBucket<String> verifyCodeSendCache = redissonClient.getBucket(SysConst.RedisPrefix.SEND_VERIFY_CODE_60 + sendVerifyCodeDto.getPhone());
        //手机号和验证码映射
        RBucket<String> verifyCodeCache = redissonClient.getBucket(SysConst.RedisPrefix.SEND_VERIFY_PHONE_MAP + sendVerifyCodeDto.getPhone());

        //1. 根据手机号判断60s内是否发过验证码，发过则抛出异常
        if (verifyCodeSendCache.isExists()) {
            log.error("{} 频繁亲求验证码接口，已被拒绝。",sendVerifyCodeDto.getPhone());
            throw new ServiceException("请不要频繁发送验证码");
        }
        //2. 产生6位数随机数作为验证码
        String randomStr = RandomStringUtils.randomNumeric(6);

        //3. 根据场景值 调用API发验证码
        if (sendVerifyCodeDto.getSceneCode() ==  SendVerifyCodeDto.SCENCE_CODE_REGISTER) {
            sendRegisterCode(randomStr, sendVerifyCodeDto.getPhone());
        } else if (sendVerifyCodeDto.getSceneCode() == SendVerifyCodeDto.SCENCE_CODE_FORGET_PASSWORD) {
            sendForgetPasswordCode(randomStr, sendVerifyCodeDto.getPhone());
        }

        //放入缓存
        verifyCodeSendCache.set(randomStr, 60, TimeUnit.SECONDS);
        verifyCodeCache.set(randomStr, 10, TimeUnit.MINUTES);
        log.info("短信验证码：{}，场景：{}", randomStr, sendVerifyCodeDto.getSceneCode());

        //3.
        //smsApi.sendVerifyCode(sendVerifyCodeDto.getPhone(), randomStr);
        //String message = "【Leif】网址验证码为" + randomStr + ",验证码10分钟内有效。";
        //发送到sms_queue的消息队列中
        //rabbitTemplate.convertAndSend("sms_queue", new SendSmsDto(sendVerifyCodeDto.getPhone(), message));

    }

    /**
     * 发送忘记密码验证码
     * @param randomStr
     * @param phone
     */
    private void sendForgetPasswordCode(String randomStr, String phone) {
        //忘记密码中的phone值为token，需要根据token从redis中获取真正的电话号码
        RBucket<String> rBucket = redissonClient.getBucket(SysConst.RedisPrefix.FORGET_PASSWORD_SEND_VERIFY + phone);
        if (!rBucket.isExists()) {
            throw new ServiceException("发送验证码异常，请重新验证用户");
        }

        //获取真正的电话号码
        String phoneNum = rBucket.get();
        String message = "【Leif】您正在修改密码，验证码为" + randomStr + ",验证码10分钟内有效。";
        //放到短信发送队列
        rabbitTemplate.convertAndSend(SysConst.MqQueueName.SMS_QUEUE, new SendSmsDto(phoneNum, message));
    }

    /**
     * 发送注册验证码
     * @param randomStr
     * @param phone
     */
    private void sendRegisterCode(String randomStr, String phone) {
        String message = "【Leif】网址注册码为" + randomStr + ",验证码10分钟内有效。";
        //放到短信发送队列
        rabbitTemplate.convertAndSend(SysConst.MqQueueName.SMS_QUEUE, new SendSmsDto(phone, message));
    }
}
