package com.aistudio.service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaptchaResponse {
    private String captchaKey;
    private String captchaImage;
}
