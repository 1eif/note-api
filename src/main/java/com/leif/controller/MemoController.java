package com.leif.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.leif.model.dto.request.CreateMemoDto;
import com.leif.model.dto.request.EditMemoDto;
import com.leif.model.dto.respons.CreateMemoRespDto;
import com.leif.model.dto.respons.DailyMemoCountRespDto;
import com.leif.model.entity.Memo;
import com.leif.service.MemoService;
import com.leif.util.QiniuUtil;
import com.leif.util.result.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/memo")
public class MemoController {

    @Autowired
    private MemoService memoService;

    @Autowired
    private QiniuUtil qiniuUtil;

    /**
     * 新建Memo
     * @param createMemoDto
     * @return
     */
    @PostMapping
    public ApiResult createMemo(@RequestBody CreateMemoDto createMemoDto) {
        createMemoDto.setUserId(StpUtil.getLoginIdAsString());
        CreateMemoRespDto createMemoRespDto = memoService.createNewMemo(createMemoDto);
        return ApiResult.SUCCESS(createMemoRespDto);
    }

    /**
     * 显示当前用户所有Tag
     * @return
     */
    @PostMapping("/tags")
    public ApiResult showTagList() {
        String userId = StpUtil.getLoginIdAsString();
        List<String> tagNames = memoService.findUserTagNames(userId);
        return ApiResult.SUCCESS(tagNames);
    }

    /**
     * 查询用户所有Memo queryTag为null查所有  不为空查指定tag的memo
     * @param queryTag
     * @return
     */
    @PostMapping("/list")
    public ApiResult showAllMemo(String queryTag) {
        String userId = StpUtil.getLoginIdAsString();
        List<Memo> memoList = memoService.findAllMemo(userId, queryTag);
        return ApiResult.SUCCESS(memoList);
    }

    /**
     * 删除Memo
     * @param memoId
     * @return
     */
    @PostMapping("/del")
    public ApiResult delMemo(String memoId) {
        String userId = StpUtil.getLoginIdAsString();
        memoService.delMemo(userId, memoId);
        return ApiResult.SUCCESS();

    }

    /**
     * 编辑Memo
     * @param editMemoDto
     * @return
     */
    @PostMapping("/edit")
    public ApiResult editMemo(@RequestBody EditMemoDto editMemoDto) {
        String userId = StpUtil.getLoginIdAsString();
        editMemoDto.setUserId(userId);
        Memo memo = memoService.editMemo(editMemoDto);
        return ApiResult.SUCCESS(memo);
    }

    /**
     * 查询60日内Memo数量
     * @return
     */
    @PostMapping("/daily_count")
    public ApiResult dailyMemoCount() {
        String userId = StpUtil.getLoginIdAsString();
        DailyMemoCountRespDto dailyMemoCountRespDto = memoService.dailyMemoCount(userId);
        return ApiResult.SUCCESS(dailyMemoCountRespDto);
    }

    /**
     * 获取文件上传token
     * @return
     */
    @PostMapping("/file/token")
    public ApiResult updateFile() {
        String token = qiniuUtil.getUploadToken();
        return ApiResult.SUCCESS(token);
    }

}
