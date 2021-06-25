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

    /**
     * 明文信息处理
     * @param message
     * @return
     */
    String callbackEvent(String message);

    /**
     * 密文信息处理
     * @param message
     * @param nonce
     * @param timeStamp
     * @param msgSigature
     * @return
     */
    String callbackEvent(String message, String nonce, String timeStamp, String msgSigature);
}
