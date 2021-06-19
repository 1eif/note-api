package com.leif.util;

/**
 * 系统常量
 */
public interface SysConst {

    /**
     * 用户密码 MD5盐值前缀
     */
    String USER_PASSWORD_SALT = "aaabbb{{{<<<*";

    /**
     * MQ名
     */
    interface MqQueueName {
        /**
         * 发送短信MQ
         */
        String SMS_QUEUE = "sms_queue";
    }

    /**
     * redis key前缀
     */
    interface RedisPrefix {
        /**
         * 60S内是否发过短信
         */
        String SEND_VERIFY_CODE_60 = "sms:verify:send:";
        /**
         * 验证码和手机号映射
         */
        String SEND_VERIFY_PHONE_MAP = "sms:verify:";
        /**
         * 图片验证码
         */
        String IMAGE_VERIFY_CODE = "image:verify:code:";
        /**
         * 忘记密码：发送手机验证码
         */
        String FORGET_PASSWORD_SEND_VERIFY = "forget:send:verify:code:";
    }
}
