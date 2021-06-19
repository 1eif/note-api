package com.leif.service;

import com.leif.model.dto.request.UserLoginDto;
import com.leif.model.dto.request.UserRegisterDto;
import com.leif.model.dto.request.ForgetPasswordSetNewPasswordDto;
import com.leif.model.dto.request.ForgetPasswordValidateUserDto;
import com.leif.model.dto.request.ForgetPasswordValidateVerifyCodeDto;
import com.leif.model.dto.request.UserSettingDto;
import com.leif.model.dto.respons.UserInfoRespDto;
import com.leif.model.entity.User;

public interface UserService {

    /**
     * 用户注册
     *
     * @param userRegisterDto
     */
    void userRegister(UserRegisterDto userRegisterDto);

    /**
     * 用户登录
     * @param userLoginDto
     * @return
     */
    User login(UserLoginDto userLoginDto);

    /**
     * 修改昵称或密码
     * @param userSettingDto
     */
    void changeNickNameAndPassword(UserSettingDto userSettingDto);

    /**
     * 用户详细信息
     * @param userID
     * @return
     */
    UserInfoRespDto getUserInfo(String userID);

    /**
     * 忘记密码：验证用户有效性
     * @param forgetPasswordValidateUserDto
     * @return
     */
    String forgetValidateUser(ForgetPasswordValidateUserDto forgetPasswordValidateUserDto);

    /**
     * 忘记密码：验证验证码有效性
     * @param forgetPasswordValidateVerifyCodeDto
     */
    void forgetValidateVerifyCode(ForgetPasswordValidateVerifyCodeDto forgetPasswordValidateVerifyCodeDto);

    /**
     * 忘记秘密：设置新密码
     * @param forgetPasswordSetNewPasswordDto
     */
    void forgetSetNewPassword(ForgetPasswordSetNewPasswordDto forgetPasswordSetNewPasswordDto);

}
