package com.leif.service;


public interface WeChatService {

    /**
     * 校验签名
     * @param signature
     * @param nonce
     * @param timeStamp
     * @return
     */
    String checkSignature(String signature, String nonce, String timeStamp);

    /**
     * 获取AccessToken
     * @return
     */
    String getAccessToken();

    /**
     * 根据用户id获取特定二维码
     * @param userId
     * @return
     */
    String sceneQrcode(String userId);
}
