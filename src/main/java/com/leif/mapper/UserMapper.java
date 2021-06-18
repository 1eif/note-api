package com.leif.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leif.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
