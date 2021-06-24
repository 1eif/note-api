package com.leif.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.leif.model.dto.request.UserLoginDto;
import com.leif.model.dto.request.UserRegisterDto;
import com.leif.model.dto.request.ForgetPasswordSetNewPasswordDto;
import com.leif.model.dto.request.ForgetPasswordValidateUserDto;
import com.leif.model.dto.request.ForgetPasswordValidateVerifyCodeDto;
import com.leif.model.dto.request.UserSettingDto;
import com.leif.model.dto.respons.UserInfoRespDto;
import com.leif.model.entity.User;
import com.leif.service.UserService;
import com.leif.service.WeChatService;
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

    @Autowired
    private WeChatService weChatService;

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

    /**
     * 安全退出
     * @return
     */
    @PostMapping("/logout")
    public ApiResult logout() {
        log.info("用户：{}安全退出系统 Device：{}",StpUtil.getLoginId(), StpUtil.getLoginDevice());
        StpUtil.logout();
        return ApiResult.SUCCESS("退出成功");
    }

    /**
     * 修改用户信息 昵称 密码
     * @param userSettingDto
     * @return
     */
    @PostMapping("/user/setting")
    public ApiResult updateUser(@RequestBody UserSettingDto userSettingDto) {
        //获取当前登录用户ID
        String userId = StpUtil.getLoginIdAsString();
        userSettingDto.setUserId(userId);
        userService.changeNickNameAndPassword(userSettingDto);
        return ApiResult.SUCCESS();
    }

    /**
     * 获取用户详细信息
     * @return
     */
    @PostMapping("/user/info")
    public ApiResult userInfo() {
        String userID = StpUtil.getLoginIdAsString();
        UserInfoRespDto userInfoRespDto = userService.getUserInfo(userID);
        return ApiResult.SUCCESS(userInfoRespDto);
    }

    @PostMapping("/user/wx/qrcode")
    public ApiResult getWeChatQrcode() {
        String userID = StpUtil.getLoginIdAsString();
        String qrCodeUrl = weChatService.sceneQrcode(userID);
        return ApiResult.SUCCESS(qrCodeUrl);
    }

    /**
     * 忘记密码：验证用户有效性
     * @param forgetPasswordValidateUserDto
     * @return
     */
    @PostMapping("/forget/validate_user")
    public ApiResult forgetValidateUser(@RequestBody ForgetPasswordValidateUserDto forgetPasswordValidateUserDto) {
        String token = userService.forgetValidateUser(forgetPasswordValidateUserDto);
        return ApiResult.SUCCESS(token);
    }

    /**
     * 忘记密码：验证验证码有效性
     * @param forgetPasswordValidateVerifyCodeDto
     * @return
     */
    @PostMapping("/forget/validate_verify_code")
    public ApiResult  forgetValidateVerifyCode(@RequestBody ForgetPasswordValidateVerifyCodeDto forgetPasswordValidateVerifyCodeDto) {
        userService.forgetValidateVerifyCode(forgetPasswordValidateVerifyCodeDto);
        return ApiResult.SUCCESS();
    }

    /**
     * 忘记密码：重新设置密码
     * @param forgetPasswordSetNewPasswordDto
     * @return
     */
    @PostMapping("/forget/new_password")
    public ApiResult forgetSetNewPassword(@RequestBody ForgetPasswordSetNewPasswordDto forgetPasswordSetNewPasswordDto) {
        userService.forgetSetNewPassword(forgetPasswordSetNewPasswordDto);
        return ApiResult.SUCCESS();
    }



}
