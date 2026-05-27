package com.aistudio.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SiteConfigUpdateRequest {

    @NotBlank(message = "站点名称不能为空")
    private String siteName;

    private String siteDescription;
    private String logoUrl;
    private String iconUrl;
    private String footerText;
    private String footerCopyright;
    private String footerRecord;
}
