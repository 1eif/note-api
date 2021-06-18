package com.leif.service;

import com.leif.model.dto.UserRegisterDto;
import com.leif.model.entity.User;

public interface UserService {

    /**
     * 用户注册
     *
     * @param userRegisterDto
     */
    void userRegister(UserRegisterDto userRegisterDto);
    /**
     *
     * 用户登录 TODO
     */
    //User login() {}
}
