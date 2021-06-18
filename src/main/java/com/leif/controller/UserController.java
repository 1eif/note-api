package com.leif.controller;

import com.leif.model.dto.UserRegisterDto;
import com.leif.service.UserService;
import com.leif.util.result.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiResult register(@RequestBody UserRegisterDto userRegisterDto) {
        userService.userRegister(userRegisterDto);
        return ApiResult.SUCCESS();

    }
}
