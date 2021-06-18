package com.leif.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.leif.model.dto.UserLoginDto;
import com.leif.model.dto.UserRegisterDto;
import com.leif.model.entity.User;
import com.leif.service.UserService;
import com.leif.util.result.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterDto
     * @return
     */
    @PostMapping("/register")
    public ApiResult register(@RequestBody UserRegisterDto userRegisterDto) {
        userService.userRegister(userRegisterDto);
        return ApiResult.SUCCESS();
    }

    /**
     * 用户登录
     * @param userLoginDto
     * @return
     */
    @PostMapping("/login")
    public ApiResult login(@RequestBody UserLoginDto userLoginDto) {
        User user = userService.login(userLoginDto);
        StpUtil.login(user.getId(), userLoginDto.getDevice());
        SaTokenInfo saTokenInfo = StpUtil.getTokenInfo();
        return ApiResult.SUCCESS(saTokenInfo);

        //return ApiResult.SUCCESS(user);
    }

    @PostMapping("/logout")
    public ApiResult logout() {
        log.info("用户：{}安全退出系统 Device：{}",StpUtil.getLoginId(), StpUtil.getLoginDevice());
        StpUtil.logout();
        return ApiResult.SUCCESS("退出成功");
    }
}
