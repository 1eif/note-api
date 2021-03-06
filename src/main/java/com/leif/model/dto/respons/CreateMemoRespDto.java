package com.leif.model.dto.respons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMemoRespDto {

    private String content;
    private String createTime;
    private String memoId;
    private String device;
    private String parentMemoId;
    private List<String> tags;

}
