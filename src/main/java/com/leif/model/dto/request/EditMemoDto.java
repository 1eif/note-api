package com.leif.model.dto.request;

import lombok.Data;

@Data
public class EditMemoDto {

    private String memoId;
    private String content;
    private String userId;
    private String device;
}
