package com.aistudio.service.service;

import com.aistudio.service.dto.request.SiteConfigUpdateRequest;
import com.aistudio.service.dto.response.SiteConfigVO;

public interface SiteConfigService {

    SiteConfigVO getConfig();

    void updateConfig(SiteConfigUpdateRequest request);
}
