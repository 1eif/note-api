package com.leif.service;

import com.leif.model.dto.respons.ImageVerificationCodeRespDto;

/**
 * 图片验证码业务层
 */
public interface ImageVerificationCodeService {

    /**
     * 生成图片
     * @return
     */
    ImageVerificationCodeRespDto generatorImage();
}
