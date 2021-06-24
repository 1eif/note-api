package com.leif.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat")
@Data
public class WeChatConfigProperty {

    private String appId;
    private String appSecret;
    private String token;
    private String aesKey;
    private String id;
}
