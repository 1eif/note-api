package com.leif.model.dto.respons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageVerificationCodeRespDto {

    private String tokenId;
    private String images;
}
