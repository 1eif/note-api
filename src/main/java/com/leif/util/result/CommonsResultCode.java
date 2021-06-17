package com.leif.util.result;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public enum CommonsResultCode implements ResultCode{

    SUCCESS(true, 0, "success"),
    FAIL(false, -1000, "error"),
    LOGIN_FAIL(false, -1001, "账号或密码错误");

    private boolean success;
    private Integer code;
    private String message;

    /**
     * 操作是否成功，true为成功，false为失败
     *
     * @return
     */
    @Override
    public boolean isSuccess() {
        return success;
    }//这个success是个变量名

    /**
     * 操作码
     *
     * @return
     */
    @Override
    public Integer getCode() {
        return code;
    }

    /**
     * 提示信息
     *
     * @return
     */
    @Override
    public String getMessage() {
        return message;
    }
}
