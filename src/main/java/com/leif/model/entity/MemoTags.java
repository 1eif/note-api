package com.leif.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("memo_tags")
public class MemoTags {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String memoId;
    private String tagsId;
}
