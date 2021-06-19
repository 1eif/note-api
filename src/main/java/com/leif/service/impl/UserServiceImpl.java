package com.leif.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leif.exception.ServiceException;
import com.leif.mapper.UserMapper;
import com.leif.model.dto.UserLoginDto;
import com.leif.model.dto.UserRegisterDto;
import com.leif.model.dto.request.ForgetPasswordSetNewPasswordDto;
import com.leif.model.dto.request.ForgetPasswordValidateUserDto;
import com.leif.model.dto.request.ForgetPasswordValidateVerifyCodeDto;
import com.leif.model.entity.User;
import com.leif.service.UserService;
import com.leif.util.DateTimeUtil;
import com.leif.util.SysConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserMapper userMapper;
    /**
     * 用户注册
     *
     * @param userRegisterDto
     */
    @Override
    public void userRegister(UserRegisterDto userRegisterDto) {
        //1. 验证验证码是否正确
        RBucket<String> rBucket = redissonClient.getBucket("sms:verify:"+userRegisterDto.getPhone());
//        if (rBucket.isExists()) {
//
//        } else {
//            throw new ServiceException("验证码错误");
//        }
        if (!StringUtils.equals(userRegisterDto.getVerifyCode(),rBucket.get())) {
            throw new ServiceException("验证码错误");
        } else {
            //验证码正确，删除。
            rBucket.delete();
        }

        //2. 判断手机号是否被注册过
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone",userRegisterDto.getPhone()));
        if (user != null) {
            throw new ServiceException("手机号码已被注册");
        }

        //3. 保存账号
        user = new User();
        user.setCreateTime(DateTimeUtil.getNowString());
        user.setPhone(userRegisterDto.getPhone());
        user.setNickName(userRegisterDto.getPhone());
        user.setStatus(User.STATUS_NORMAL);
        user.setPassword(DigestUtils.md5Hex(SysConst.USER_PASSWORD_SALT + userRegisterDto.getPassword()));
        log.info("账号：{} 注册成功",userRegisterDto.getPhone());

        userMapper.insert(user);
    }

    /**
     * 用户登录
     *
     * @param userLoginDto
     * @return
     */
    @Override
    public User login(UserLoginDto userLoginDto) {
        //TODO 处理频繁登录错误的请求
        //1. 根据手机号码查询User对象
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", userLoginDto.getPhone()));
        if (user == null) {
            throw new ServiceException("账号或密码错误");
        }

        //2. 根据User中密码和数据库中密码比对
        if (!StringUtils.equals(user.getPassword(), DigestUtils.md5Hex(SysConst.USER_PASSWORD_SALT +userLoginDto.getPassword()))) {
            throw new ServiceException("账号或密码错误");
        }

        log.info("用户：{} 成功登录系统， device：{}", userLoginDto.getPhone(), userLoginDto.getDevice());
        return user;
    }

    /**
     * 忘记密码：验证用户有效性
     * @param forgetPasswordValidateUserDto
     * @return
     */
    @Override
    public String forgetValidateUser(ForgetPasswordValidateUserDto forgetPasswordValidateUserDto) {
        //1. 判断验证码是否正确
        RBucket<String> rBucket = redissonClient.getBucket(SysConst.RedisPrefix.IMAGE_VERIFY_CODE + forgetPasswordValidateUserDto.getTokenID());
        if (!StringUtils.equals(forgetPasswordValidateUserDto.getVerifyCode(), rBucket.get())) {
            throw new ServiceException("验证码错误");
        } else {
            rBucket.delete();
        }

        //2. 根据手机号查询账户是否存在
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", forgetPasswordValidateUserDto.getPhone()));
        if (user == null) {
            throw new ServiceException("账号不存在");
        }

        //3. 用户存在 创建发送短信的token，用于之后发送短信时验证
        String token = UUID.randomUUID().toString().replace("-", "");
        RBucket<String> sendVerifyCodeCache = redissonClient.getBucket(SysConst.RedisPrefix.FORGET_PASSWORD_SEND_VERIFY + token);
        sendVerifyCodeCache.set(user.getPhone(), 10, TimeUnit.MINUTES);

        return token;

    }

    /**
     * 忘记密码：验证验证码有效性
     * @param forgetPasswordValidateVerifyCodeDto
     */
    @Override
    public void forgetValidateVerifyCode(ForgetPasswordValidateVerifyCodeDto forgetPasswordValidateVerifyCodeDto) {
        RBucket<String> rBucket = redissonClient.getBucket(SysConst.RedisPrefix.SEND_VERIFY_PHONE_MAP + forgetPasswordValidateVerifyCodeDto.getToken());
        if (!StringUtils.equals(forgetPasswordValidateVerifyCodeDto.getVerifyCode(), rBucket.get())) {
            throw new ServiceException("验证码错误");
        } else {
            rBucket.delete();
        }
    }

    /**
     * 忘记密码：设置新密码
     * @param forgetPasswordSetNewPasswordDto
     */
    @Override
    public void forgetSetNewPassword(ForgetPasswordSetNewPasswordDto forgetPasswordSetNewPasswordDto) {
        RBucket<String> rBucket = redissonClient.getBucket(SysConst.RedisPrefix.FORGET_PASSWORD_SEND_VERIFY + forgetPasswordSetNewPasswordDto.getToken());
        if (!rBucket.isExists()) {
            throw new ServiceException("验证码过期，请重新获取");
        }

        String phone = rBucket.get();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        rBucket.delete();

        user.setPassword(DigestUtils.md5Hex(SysConst.USER_PASSWORD_SALT + forgetPasswordSetNewPasswordDto.getPassword()));
        userMapper.updateById(user);
        log.info("用户：{}通过忘记秘密重新设置密码成功",user.getId());
    }


}
