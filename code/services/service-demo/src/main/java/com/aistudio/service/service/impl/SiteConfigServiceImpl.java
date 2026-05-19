package com.aistudio.service.service.impl;

import com.aistudio.service.dto.response.SiteConfigVO;
import com.aistudio.service.entity.SiteConfig;
import com.aistudio.service.mapper.SiteConfigMapper;
import com.aistudio.service.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteConfigServiceImpl implements SiteConfigService {

    private static final Long DEFAULT_ID = 1L;

    private final SiteConfigMapper siteConfigMapper;

    @Override
    public SiteConfigVO getConfig() {
        SiteConfig config = siteConfigMapper.selectById(DEFAULT_ID);
        if (config == null) {
            config = createDefaultConfig();
        }
        SiteConfigVO vo = new SiteConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }

    private SiteConfig createDefaultConfig() {
        SiteConfig config = new SiteConfig();
        config.setId(DEFAULT_ID);
        config.setSiteName("AI Studio Template");
        config.setSiteDescription("Minimal console template");
        config.setLogoUrl("/ai-studio-logo.svg");
        config.setIconUrl("/favicon.png");
        config.setFooterText("AI Studio Template");
        config.setFooterCopyright("Copyright 2026");
        config.setFooterRecord("");
        siteConfigMapper.insert(config);
        return config;
    }
}
