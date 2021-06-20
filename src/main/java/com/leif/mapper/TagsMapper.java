package com.leif.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leif.model.entity.Tags;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagsMapper extends BaseMapper<Tags> {
}
