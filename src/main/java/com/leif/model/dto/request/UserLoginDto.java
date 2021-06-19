package com.leif.model.dto.request;

import lombok.Data;

@Data
public class UserLoginDto {

    private String phone;
    private String password;
    private String device;
}
