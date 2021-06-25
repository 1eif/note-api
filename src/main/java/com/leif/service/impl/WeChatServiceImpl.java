package com.leif.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leif.config.WeChatConfigProperty;
import com.leif.exception.ServiceException;
import com.leif.mapper.MemoMapper;
import com.leif.mapper.UserMapper;
import com.leif.model.entity.Memo;
import com.leif.model.entity.User;
import com.leif.service.WeChatService;
import com.leif.util.DateTimeUtil;
import com.leif.util.HttpUtil;
import com.leif.util.SysConst;
import com.leif.util.XmlUtil;
import com.leif.util.wechat.AesException;
import com.leif.util.wechat.WXBizMsgCrypt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WeChatServiceImpl implements WeChatService {

    @Autowired
    private WeChatConfigProperty weChatConfigProperty;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemoMapper memoMapper;

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
        paramMap.put("action_name", "QR_STR_SCENE");

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

    /**
     * 明文信息处理
     * @param message
     * @return
     */
    @Override
    public String callbackEvent(String message) {
        log.info("微信明文回调内容：{}", message);
        Map<String, String> xmlMap = XmlUtil.parseXmlToMap(message);
        //如果MsgType为event 且event为SCAN或subscribe
        if (StringUtils.equals(xmlMap.get("MsgType"), "event") && StringUtils.equalsAny(xmlMap.get("Event"), "SCAN", "subscribe")) {
            //扫描二维码事件  ： 绑定用户
            try {
                bindUserWeChat(xmlMap.get("EventKey"), xmlMap.get("FromUserName"));
                return sendTextMessageToUser(xmlMap.get("FromUserName"), "绑定成功");
            } catch (ServiceException e) {
                return sendTextMessageToUser(xmlMap.get("FromUserName"), e.getMessage());
            }
        } else if (StringUtils.equals(xmlMap.get("MsgType"), "text")) {
            //普通文本消息 ： 新建Memo
            try {
                saveTextMessage(xmlMap.get("FromUserName"), xmlMap.get("Content"));
                return sendTextMessageToUser(xmlMap.get("FromUserName"), "保存Memo成功");
            } catch (ServiceException e) {
                return sendTextMessageToUser(xmlMap.get("FromUserName"), e.getMessage());
            }
        }
        return null;
    }

    /**
     * 密文信息处理
     * @param message
     * @param nonce
     * @param timeStamp
     * @param msgSigature
     * @return
     */
    @Override
    public String callbackEvent(String message, String nonce, String timeStamp, String msgSigature) {
        log.info("微信密文回调内容：{}", message);

        return decodeCallbackEvent(message, nonce, timeStamp, msgSigature);
    }

    /**
     * 密文传输处理
     * @param message
     * @param nonce
     * @param timeStamp
     * @param msgSigature
     * @return
     */
    private String decodeCallbackEvent(String message, String nonce, String timeStamp, String msgSigature) {
        try {
            WXBizMsgCrypt wxBizMsgCrypt = getWXBizMsgCrypt();
            String decryptMsg = wxBizMsgCrypt.decryptMsg(msgSigature, timeStamp, nonce, message);
            log.info("微信密文解析后明文：{}", decryptMsg);

            Map<String, String> xmlMap = XmlUtil.parseXmlToMap(decryptMsg);
            //如果MsgType为event 且event为SCAN或subscribe
            if (StringUtils.equals(xmlMap.get("MsgType"), "event") && StringUtils.equalsAny(xmlMap.get("Event"), "SCAN", "subscribe")) {
                //扫描二维码事件  ： 绑定用户
                try {
                    bindUserWeChat(xmlMap.get("EventKey"), xmlMap.get("FromUserName"));
                    return wxBizMsgCrypt.encryptMsg(sendTextMessageToUser(xmlMap.get("FromUserName"), "绑定成功"), timeStamp, nonce);
                } catch (ServiceException e) {
                    return wxBizMsgCrypt.encryptMsg(sendTextMessageToUser(xmlMap.get("FromUserName"), e.getMessage()), timeStamp, nonce);
                }
            } else if (StringUtils.equals(xmlMap.get("MsgType"), "text")) {
                //普通文本消息 ： 新建Memo
                try {
                    saveTextMessage(xmlMap.get("FromUserName"), xmlMap.get("Content"));
                    return wxBizMsgCrypt.encryptMsg(sendTextMessageToUser(xmlMap.get("FromUserName"), "保存Memo成功"), timeStamp, nonce);
                } catch (ServiceException e) {
                    return wxBizMsgCrypt.encryptMsg(sendTextMessageToUser(xmlMap.get("FromUserName"), e.getMessage()), timeStamp, nonce);
                }
            }
            return "";

        } catch (AesException e) {
            throw new ServiceException("微信密文解密失败：{}", e);
        }
    }

    private WXBizMsgCrypt getWXBizMsgCrypt() throws AesException {
        return new WXBizMsgCrypt(weChatConfigProperty.getToken(), weChatConfigProperty.getAesKey(), weChatConfigProperty.getAppId());
    }

    /**
     * 绑定微信openId
     * @param userId
     * @param openId
     */
    private void bindUserWeChat(String userId, String openId) {
        if (StringUtils.startsWith(userId, "qrscene_")) {
            userId = StringUtils.substringAfter(userId, "qrscene_");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在，绑定微信失败:" + userId);
        }

        user.setWxOpenId(openId);
        userMapper.updateById(user);
        log.info("用户：{}绑定微信：{}成功", userId, openId);
    }

    /**
     * 给微信回复文本消息
     * @param openId
     * @param message
     * @return
     */
    private String sendTextMessageToUser(String openId, String message) {
        String result =
                "<xml>\n" +
                "  <ToUserName><![CDATA[" + openId + "]]></ToUserName>\n" +
                "  <FromUserName><![CDATA[" + weChatConfigProperty.getId() + "]]></FromUserName>\n" +
                "  <CreateTime>" + new Date().getTime() + "</CreateTime>\n" +
                "  <MsgType><![CDATA[text]]></MsgType>\n" +
                "  <Content><![CDATA[" + message + "]]></Content>\n" +
                "</xml>";
        byte[] utf8Bytes = result.getBytes(StandardCharsets.UTF_8);
        try {
            return new String(utf8Bytes, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("编码错误", e);
        }

    }

    /**
     * 保存微信记录到用户Memo
     * @param openId
     * @param message
     */
    private void saveTextMessage(String openId, String message) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("wx_open_id", openId));
        if (user == null) {
            log.error("微信接收到未知用户：{}， 消息：{}", openId, message);
            throw new ServiceException("未绑定到笔记账号，无法处理该消息");
        }
        Memo memo = new Memo();
        memo.setContent(message);
        memo.setUserId(user.getId());
        memo.setDevice("wechat");
        memo.setCreateTime(DateTimeUtil.getNowString());

        memoMapper.insert(memo);
        log.info("保存微信记录到用户Memo：{}",memo);

    }
}
