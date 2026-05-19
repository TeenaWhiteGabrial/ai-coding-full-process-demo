package com.aistudio.service.controller.portal;

import com.aistudio.service.common.Result;
import com.aistudio.service.dto.response.SiteConfigVO;
import com.aistudio.service.service.SiteConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Portal Site")
@RestController
@RequestMapping("/common/site")
@RequiredArgsConstructor
public class SiteConfigController {

    private final SiteConfigService siteConfigService;

    @Operation(summary = "Get site config")
    @GetMapping("/config")
    public Result<SiteConfigVO> config() {
        return Result.success(siteConfigService.getConfig());
    }
}
