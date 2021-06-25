package com.leif.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class User {
    /**
     *
     * 账号状态：正常
     */
    public static final int STATUS_NORMAL = 0;//final 初始化后不能修改
    /**
     *
     * 账号状态：禁用
     */
    public static final int STATUS_DISABLE = 1;

    /**
     * @TableId(value, type)
     *value:
     * 主键字段名
     * type:IdType.ASSIGN_ID：
     * 分配ID(主键类型为Number(Long和Integer)或String)(since 3.3.0),使用接口IdentifierGenerator的方法nextId(默认实现类为DefaultIdentifierGenerator雪花算法)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String phone;
    private String nickName; //表中为 nick_name 此处约定改为驼峰命名法
    private String password;
    private String createTime;
    private int status;
    private String wxOpenId;


}
