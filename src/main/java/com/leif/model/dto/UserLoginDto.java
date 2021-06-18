package com.leif.model.dto;

import lombok.Data;

@Data
public class UserLoginDto {

    private String phone;
    private String password;
    private String device;
}
