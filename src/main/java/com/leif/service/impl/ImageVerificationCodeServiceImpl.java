package com.leif.service.impl;

import com.leif.exception.ServiceException;
import com.leif.model.dto.respons.ImageVerificationCodeRespDto;
import com.leif.service.ImageVerificationCodeService;
import com.leif.util.ImageVerificationCode;
import com.leif.util.SysConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ImageVerificationCodeServiceImpl implements ImageVerificationCodeService {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 生成图片
     *
     * @return
     */
    @Override
    public ImageVerificationCodeRespDto generatorImage() {
        ImageVerificationCode imageVerificationCode = new ImageVerificationCode();

        //获取验证码 图片和文本
        BufferedImage image = imageVerificationCode.getImage();
        String text = imageVerificationCode.getText();

        //将图片变为base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            //验证码图片转为字节数组
            ImageVerificationCode.output(image, outputStream);
            byte[] bytes = outputStream.toByteArray();
            outputStream.flush();
            outputStream.close();

            //将图片拼为base64
            String imageStr = "data:image/jpg;base64, " + Base64.encodeBase64String(bytes);

            //生成token
            String uuid = UUID.randomUUID().toString().replace("-", "");
            //token对应的验证码映射 放入redis
            RBucket<String> rBucket = redissonClient.getBucket(SysConst.RedisPrefix.IMAGE_VERIFY_CODE + uuid);
            //有效期10分钟
            rBucket.set(text, 10, TimeUnit.MINUTES);

            log.info("生成图片验证码成功：{}", text);
            return new ImageVerificationCodeRespDto(uuid, imageStr);

        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceException("验证码图片生成错误", e);
        }
    }
}
