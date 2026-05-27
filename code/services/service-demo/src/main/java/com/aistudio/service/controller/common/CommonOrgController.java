package com.aistudio.service.controller.common;

import com.aistudio.service.common.Result;
import com.aistudio.service.dto.request.OrgCreateRequest;
import com.aistudio.service.dto.response.OrgOptionVO;
import com.aistudio.service.dto.response.OrgTreeVO;
import com.aistudio.service.entity.SysOrg;
import com.aistudio.service.service.OrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Common Org Management")
@RestController
@RequestMapping("/common/org")
@RequiredArgsConstructor
public class CommonOrgController {

    private final OrgService orgService;

    @Operation(summary = "Organization tree")
    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OP_ADMIN')")
    public Result<List<OrgTreeVO>> tree() {
        return Result.success(orgService.listTree());
    }

    @Operation(summary = "Organization options")
    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OP_ADMIN')")
    public Result<List<OrgOptionVO>> options() {
        return Result.success(orgService.listOptions());
    }

    @Operation(summary = "Organization detail")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OP_ADMIN')")
    public Result<SysOrg> detail(@PathVariable Long id) {
        return Result.success(orgService.getById(id));
    }

    @Operation(summary = "Create organization")
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Long> create(@Valid @RequestBody OrgCreateRequest request) {
        return Result.success(orgService.create(request));
    }

    @Operation(summary = "Update organization")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OrgCreateRequest request) {
        orgService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "Delete organization")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        orgService.delete(id);
        return Result.success();
    }
}
