package com.aistudio.service.controller.common;

import com.aistudio.service.common.Result;
import com.aistudio.service.dto.request.SiteConfigUpdateRequest;
import com.aistudio.service.dto.response.SiteConfigVO;
import com.aistudio.service.service.SiteConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Common Site Management")
@RestController
@RequestMapping("/common/site")
@RequiredArgsConstructor
public class CommonSiteConfigController {

    private final SiteConfigService siteConfigService;

    @Operation(summary = "Get site config for management")
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<SiteConfigVO> detail() {
        return Result.success(siteConfigService.getConfig());
    }

    @Operation(summary = "Update site config")
    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> update(@Valid @RequestBody SiteConfigUpdateRequest request) {
        siteConfigService.updateConfig(request);
        return Result.success();
    }
}
