package com.leif.service;

import com.leif.model.dto.request.CreateMemoDto;
import com.leif.model.dto.request.EditMemoDto;
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

    /**
     * 根据MemoId删除该用户的Memo
     * @param userId
     * @param memoId
     */
    void delMemo(String userId, String memoId);

    /**
     * 修改Memo
     * @param editMemoDto
     * @return
     */
    Memo editMemo(EditMemoDto editMemoDto);
}
