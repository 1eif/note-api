package com.leif.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

@Slf4j
public class HttpUtil {

    public static String sendGetRequest(String url) {
        //构建请求
        Request request = new Request.Builder().url(url).build();
        return getRequestString(url, request);

    }

    public static String sendPostRequestWithJsonBody(String url, String json) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"),json);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        return getRequestString(url, request);
    }

    private static String getRequestString(String url, Request request) {
        OkHttpClient okHttpClient = getOkHttpClient();
        //发送请求
        try (Response response = okHttpClient.newCall(request).execute()) {
            String result = response.body().string();
            log.info("http请求：{}成功，结果：{}", url, result);
            return result;
        }catch (IOException e) {
            log.error("http请求：{}异常，结果：{}", url, e);
            throw new RuntimeException("请求：" + url + "异常", e);
        }
    }

    /**
     * 创建一个okhttp客户端
     * @return
     */
    private static OkHttpClient getOkHttpClient() {
        return new OkHttpClient();
    }
}
