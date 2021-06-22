package com.leif.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leif.model.entity.Memo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MemoMapper extends BaseMapper<Memo> {
    /**
     * 三表联查
     * 先在tags表中 根据tag名查tag对应的id(tagsId)
     * 再根据tagsId在memo_tags表中查对应的memoId(id)
     * 再根据id在memo表中查内容
     *
     * @param userId
     * @param tagName
     * @return
     */
    List<Memo> findMemoByTagName(@Param("userId") String userId, @Param("tagName") String tagName);

    List<Map> findDailyCount(@Param("userId") String userId);
}
