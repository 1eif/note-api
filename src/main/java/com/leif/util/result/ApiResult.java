package com.leif.util.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)//返回值转成的Json中只包含有内容的数据
public class ApiResult<T> {//泛型类
    private boolean success;
    private Integer code;
    private String message;
    private T data;//泛型

    //构造--开始
    public ApiResult() {

    }

    public ApiResult(ResultCode resultCode) {
        this(resultCode, null);
    }

    public ApiResult(T data) {
        this(CommonsResultCode.SUCCESS, data);
    }

    public ApiResult(ResultCode resultCode, T data) {
        this.success = resultCode.isSuccess();
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }
    //构造--结束


    /**
     * 操作成功
     *
     * @return
     */
    public static ApiResult SUCCESS() {
        return new ApiResult(CommonsResultCode.SUCCESS);
    }

    public static <T>ApiResult SUCCESS(T t) {
        return new ApiResult(CommonsResultCode.SUCCESS, t);
    }

    /**
     * 操作失败
     *
     * @return
     */
    public static ApiResult FAIL() { return new ApiResult(CommonsResultCode.FAIL); }

    public static ApiResult FAIL(String message) {
        ApiResult responseResult = new ApiResult();
        responseResult.success = false;
        responseResult.code = -1;
        responseResult.message = message;
        return  responseResult;
    }

    public static ApiResult FAIL(Integer code, String message) {
        ApiResult responseResult = new ApiResult();
        responseResult.success = false;
        responseResult.code = code;
        responseResult.message = message;
        return  responseResult;
    }



}
