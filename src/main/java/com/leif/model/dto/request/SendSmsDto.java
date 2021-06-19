package com.leif.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
//添加构造方法
@AllArgsConstructor
@NoArgsConstructor
public class SendSmsDto implements Serializable {
    /**
     * 序列化是指把一个Java对象变成二进制内容，本质上就是一个byte[]数组。
     *
     * 为什么要把Java对象序列化呢？因为序列化后可以把byte[]保存到文件中，或者把byte[]通过网络传输到远程，这样，就相当于把Java对象存储到文件或者通过网络传输出去了。
     *
     * 有序列化，就有反序列化，即把一个二进制内容（也就是byte[]数组）变回Java对象。有了反序列化，保存到文件中的byte[]数组又可以“变回”Java对象，或者从网络上读取byte[]并把它“变回”Java对象。
     */
    private String phone;
    private String message;
}
