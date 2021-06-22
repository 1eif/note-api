package com.leif.model.dto.request;

import com.leif.model.entity.MemoFiles;
import lombok.Data;

@Data
public class EditMemoDto {

    private String memoId;
    private String content;
    private String userId;
    private String device;

    /**
     * 关联的文件
     */
    public MemoFiles[] fileList;
}
