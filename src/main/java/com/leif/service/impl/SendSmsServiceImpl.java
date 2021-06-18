package com.leif.service.impl;

import com.leif.exception.ServiceException;
import com.leif.model.dto.SendSmsDto;
import com.leif.model.dto.SendVerifyCodeDto;
import com.leif.service.SendSmsService;
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
        RBucket<String> verifyCodeSendCache = redissonClient.getBucket("sms:verify:send:"+sendVerifyCodeDto.getPhone());
        //手机号和验证码映射
        RBucket<String> verifyCodeCache = redissonClient.getBucket("sms:verify:"+sendVerifyCodeDto.getPhone());

        //1. 根据手机号判断60s内是否发过验证码，发过则抛出异常
        if (verifyCodeSendCache.isExists()) {
            log.error("{} 频繁亲求验证码接口，已被拒绝。",sendVerifyCodeDto.getPhone());
            throw new ServiceException("请不要频繁发送验证码");
        }
        //2. 产生6位数随机数作为验证码
        String randomStr = RandomStringUtils.randomNumeric(6);
        //放入缓存
        verifyCodeSendCache.set(randomStr, 60, TimeUnit.SECONDS);
        verifyCodeCache.set(randomStr, 10, TimeUnit.MINUTES);
        log.info("短信验证码：{}", randomStr);

        //3.
        //smsApi.sendVerifyCode(sendVerifyCodeDto.getPhone(), randomStr);
        String message = "【Leif】网址注册码为" + randomStr + ",验证码10分钟内有效。";
        //发送到sms_queue的消息队列中
        rabbitTemplate.convertAndSend("sms_queue", new SendSmsDto(sendVerifyCodeDto.getPhone(), message));

    }
}
