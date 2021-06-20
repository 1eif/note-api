package com.leif.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leif.model.entity.Memo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemoMapper extends BaseMapper<Memo> {
}
