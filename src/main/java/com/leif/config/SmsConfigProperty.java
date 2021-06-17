package com.leif.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties("sms")
@Configuration
public class SmsConfigProperty {

    private String username;
    private String password;
}
