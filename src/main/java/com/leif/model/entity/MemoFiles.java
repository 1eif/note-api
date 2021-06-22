package com.leif.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("memo_files")
public class MemoFiles {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String memoId;
    private String name;
    private String fileKey;
    private Long fileSize;
    private String url;

}
