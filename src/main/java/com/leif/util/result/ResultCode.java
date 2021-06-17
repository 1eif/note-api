package com.leif.util.result;

public interface ResultCode {

    /**
     * 操作是否成功，true为成功，false为失败
     *
     * @return
     */
    boolean isSuccess();

    /**
     * 操作码
     *
     * @return
     */
    Integer getCode();

    /**
     * 提示信息
     *
     * @return
     */
    String getMessage();
}
