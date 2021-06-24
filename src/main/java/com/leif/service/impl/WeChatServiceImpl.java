package com.leif.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leif.config.WeChatConfigProperty;
import com.leif.exception.ServiceException;
import com.leif.service.WeChatService;
import com.leif.util.HttpUtil;
import com.leif.util.SysConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WeChatServiceImpl implements WeChatService {

    @Autowired
    private WeChatConfigProperty weChatConfigProperty;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 校验签名
     *
     * @param signature
     * @param nonce
     * @param timeStamp
     * @return
     */
    @Override
    public String checkSignature(String signature, String nonce, String timeStamp) {
        log.info("微信请求：signature：{}，nonce：{}，timeStamp：{}", signature, nonce, timeStamp);

        if(StringUtils.isAnyEmpty(signature, nonce, timeStamp)) {
            log.error("微信请求参数不完整");
            return "error";
        }

        String token = weChatConfigProperty.getToken();

//        * 1）将token、timestamp、nonce三个参数进行字典序排序
        List<String> list = Arrays.asList(token, timeStamp, nonce);
        Collections.sort(list);
//        * 2）将三个参数字符串拼接成一个字符串进行sha1加密
        StringBuilder stringBuilder = new StringBuilder();
        for(String str : list) {
            stringBuilder.append(str);
        }
        String key = DigestUtils.sha1Hex(stringBuilder.toString());
//        * 3）开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
        if(key.equals(signature)) {
            log.info("微信验证通过");
            return "";
        } else {
            log.error("微信验证失败");
            return "error";
        }

    }

    /**
     * 获取AccessToken
     * @return
     */
    @Override
    public String getAccessToken() {
        RBucket<String> weChatAccessTokenCache = redissonClient.getBucket(SysConst.RedisPrefix.WECHAT_ACCESS_TOKEN);
        if(weChatAccessTokenCache.isExists()) {
            return weChatAccessTokenCache.get();
        }
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + weChatConfigProperty.getAppId() + "&secret=" + weChatConfigProperty.getAppSecret();
        String result = HttpUtil.sendGetRequest(url);
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap hashMap = mapper.readValue(result, HashMap.class);
            //失败 ：{"errcode":40013,"errmsg":"invalid appid"}
            //如果errcode存在 则失败
            if (hashMap.containsKey("errcode")) {
                throw new ServiceException("获取微信AccessToken异常L" + hashMap.get("errmsg"));
            }

            //成功：{"access_token":"ACCESS_TOKEN","expires_in":7200}
            //如果access_token存在 则成功
            if (hashMap.containsKey("access_token")) {
                String accessToken = hashMap.get("access_token").toString();
                weChatAccessTokenCache.set(accessToken, Long.parseLong(hashMap.get("expires_in").toString()), TimeUnit.SECONDS);
                return accessToken;
            } else {
                throw new ServiceException("获取微信AccessToken异常");
            }

        }
        catch (JsonProcessingException e) {
            throw new ServiceException("解析Json异常", e);
        }
    }

    /**
     * 根据用户id获取特定二维码
     * @param userId
     * @return
     */
    @Override
    public String sceneQrcode(String userId) {
        String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + getAccessToken();

//        {
//            "expire_seconds": 604800,
//                "action_name": "QR_SCENE",
//                "action_info": {
//            "scene": {
//                "scene_str": "123"
//            }
//        }
//        }
        //拼Json
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("expire_seconds", 86400);//有效期一天
        paramMap.put("action_name", "QR_SCENE");

        Map<String, Object> actionInfoMap = new HashMap<>();
        Map<String, Object> sceneMap = new HashMap<>();

        sceneMap.put("scene_str", userId);
        actionInfoMap.put("scene", sceneMap);

        paramMap.put("action_info", actionInfoMap);

        ObjectMapper jsonMapper = new ObjectMapper();
        try {
            String json = jsonMapper.writeValueAsString(paramMap);
            log.info("微信场景二维码请求Json：{}", json);
            String respJson = HttpUtil.sendPostRequestWithJsonBody(url, json);
            HashMap respMap = jsonMapper.readValue(respJson, HashMap.class);
            if (respMap.containsKey("errcode")) {
                throw new ServiceException("获取微信场景二维码异常" + respMap.get("errmsg"));
            } else {
                String ticket = respMap.get("ticket").toString();
                return "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + URLEncoder.encode(ticket, "utf-8");
            }
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new ServiceException("生成Json异常", e);
        }
    }
}
