package com.leif.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.leif.model.dto.request.CreateMemoDto;
import com.leif.util.result.ApiResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/memo")
public class MemoController {


    @PostMapping
    public ApiResult createMemo(@RequestBody CreateMemoDto createMemoDto) {
        createMemoDto.setUserId(StpUtil.getLoginIdAsString());

        return ApiResult.SUCCESS();
    }
}
