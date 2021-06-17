package com.leif.exception;


import lombok.extern.slf4j.Slf4j;
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

}
