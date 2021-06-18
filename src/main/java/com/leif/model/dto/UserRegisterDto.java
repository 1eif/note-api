package com.leif.model.dto;

import lombok.Data;

@Data
public class UserRegisterDto {

    private String phone;
    private String password;
    private String verifyCode;
}
