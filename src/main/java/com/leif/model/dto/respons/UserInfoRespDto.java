package com.leif.model.dto.respons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoRespDto {

    private String userID;
    private String nickName;
    private Integer userStatus;
    private String joinDate;
    private Integer joinDays;

    private Integer tagNums;
    private Integer memoNums;

}
