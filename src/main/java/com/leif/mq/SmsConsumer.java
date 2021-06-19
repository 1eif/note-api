package com.leif.mq;


import com.leif.model.dto.request.SendSmsDto;
import com.leif.util.SysConst;
import com.leif.util.api.SmsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发送短信消费者
 */
@Component
@Slf4j
public class SmsConsumer {

    @Autowired
    private SmsApi smsApi;

    /**
     * 注册一个MQ监听
     *
     * @param dto
     */
    @RabbitListener(queuesToDeclare = @Queue(SysConst.MqQueueName.SMS_QUEUE))
    public void sendSms(SendSmsDto dto) {
        //smsApi.sendSms(dto.getPhone(), dto.getMessage());
    }

}
