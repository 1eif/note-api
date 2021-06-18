package com.leif.exception;


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
        //TODO 加入sa-token后扩充
        //instanceof 判断异常是哪个子异常
        if (ex instanceof ServiceException) {
            return ApiResult.FAIL(ex.getMessage());
        } else {
            return ApiResult.FAIL("服务器异常");
        }
    }

    //TODO 指定捕获某一类的异常
    //@ExceptionHandler(xxxx)
    //public ApiResult
}
