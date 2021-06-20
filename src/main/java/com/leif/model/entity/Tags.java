package com.leif.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Tags {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String tag;
    private String userId;
}
