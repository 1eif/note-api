package com.leif.model.dto.request;

import com.leif.model.entity.MemoFiles;
import lombok.Data;

@Data
public class CreateMemoDto {

    //内容
    private String content;
    //关联的memoid
    private String parentId;
    //设备
    private String device;
    //关联的文件ID
    private MemoFiles[] fileList;
    //当前用户id
    private String userId;

}
