package com.leif.exception;


import cn.dev33.satoken.exception.DisableLoginException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.leif.util.result.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常捕获
 * 闪念笔记-5 用户设置+安全退出 27：00
 *
 * 异常上抛 到Controller  不处理会报500错误
 */

//@ControllerAdvice         开启全局异常捕获
@RestControllerAdvice
@Slf4j
public class GlobalException {

    //所有异常
    @ExceptionHandler
    public ApiResult handlerException(Exception ex) {
        ex.printStackTrace();
        log.error("全局异常：{}",ex.getMessage());
        //sa-token异常
        //instanceof 判断异常是哪个子异常
        if (ex instanceof NotLoginException) {
            return ApiResult.FAIL(ex.getMessage());
        }
        else if (ex instanceof NotRoleException) {
            NotRoleException e = (NotRoleException) ex;
            return ApiResult.FAIL("无此角色" + e.getRole());
        }
        else if (ex instanceof NotPermissionException) {
            NotPermissionException e = (NotPermissionException) ex;
            return ApiResult.FAIL("权限不足" + e.getCode());
        }
        else if (ex instanceof DisableLoginException) {
            return ApiResult.FAIL("账号被封禁");
        }
        else if (ex instanceof ServiceException) {
            return ApiResult.FAIL(ex.getMessage());
        }
        else {
            return ApiResult.FAIL("服务器异常");
        }
    }

    //指定捕获某一类的异常
    @ExceptionHandler(NotLoginException.class)
    public ApiResult handlerNotLoginException(NotLoginException ex) {
        ex.printStackTrace();

        //判断场景值  定制化异常信息
        String message = "";
        if (ex.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = NotLoginException.NOT_TOKEN_MESSAGE;
        }
        else if (ex.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = NotLoginException.INVALID_TOKEN_MESSAGE;
        }
        else if (ex.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = NotLoginException.TOKEN_TIMEOUT_MESSAGE;
        }
        else if (ex.getType().equals(NotLoginException.BE_REPLACED)) {
            message = NotLoginException.BE_REPLACED_MESSAGE;
        }
        else if (ex.getType().equals(NotLoginException.KICK_OUT)) {
            message = NotLoginException.KICK_OUT_MESSAGE;
        }
        else {
            message = NotLoginException.DEFAULT_MESSAGE;
        }
        return ApiResult.FAIL(-2000, message);
    }
}
