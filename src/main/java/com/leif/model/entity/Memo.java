package com.leif.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Memo {

    @TableId(type = IdType.ASSIGN_ID)//雪花算法分配ID
    private String id;
    private String content;
    private String parentId;
    private String device;
    private String createTime;
    private String userId;
}
