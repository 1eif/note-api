package com.leif.model.dto.respons;

import lombok.Data;

@Data
public class UserInfoRespDto {

    private String userID;
    private String nickName;
    private Integer userStatus;
    private String joinDate;
    private Integer joinDays;

    private Integer tagNums;
    private Integer memoNums;

}
