package com.leif.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HttpUtil {
    public static String sendGetRequest(String url) {
        //创建一个客户端
        OkHttpClient client = new OkHttpClient();
        //构建请求
        Request request = new Request.Builder()
                .url(url).build();
        //发送请求
        try (Response response = client.newCall(request).execute()) {
            String result = response.body().string();
            return result;
        }catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
