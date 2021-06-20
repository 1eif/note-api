package com.leif.service;

import com.leif.model.dto.request.CreateMemoDto;
import com.leif.model.dto.respons.CreateMemoRespDto;
import com.leif.model.entity.Memo;

import java.util.List;

public interface MemoService {

    /**
     * 创建新的Memo
     * @param createMemoDto
     * @return
     */
    CreateMemoRespDto createNewMemo(CreateMemoDto createMemoDto);

    /**
     * 根据ID查询tag的集合
     * @param userId
     * @return
     */
    List<String> findUserTagNames(String userId);

    /**
     * 显示当前用户所有Memo
     * @param userId
     * @param queryTag
     * @return
     */
    List<Memo> findAllMemo(String userId, String queryTag);
}
