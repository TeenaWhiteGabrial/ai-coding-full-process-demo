package com.aistudio.service.controller.common;

import com.aistudio.service.common.Result;
import com.aistudio.service.dto.request.ResetPasswordRequest;
import com.aistudio.service.dto.request.UserCreateRequest;
import com.aistudio.service.dto.request.UserUpdateRequest;
import com.aistudio.service.dto.response.PageResult;
import com.aistudio.service.dto.response.UserManageVO;
import com.aistudio.service.entity.SysRole;
import com.aistudio.service.mapper.SysRoleMapper;
import com.aistudio.service.service.UserService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Common User Management")
@RestController
@RequestMapping("/common/user")
@RequiredArgsConstructor
public class CommonUserController {

    private final UserService userService;
    private final SysRoleMapper roleMapper;

    @Operation(summary = "User list")
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OP_ADMIN')")
    public Result<PageResult<UserManageVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orgId) {
        return Result.success(userService.listUsers(page, size, keyword, orgId));
    }

    @Operation(summary = "Role list")
    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OP_ADMIN')")
    public Result<List<SysRole>> roles() {
        return Result.success(roleMapper.selectList(null));
    }

    @Operation(summary = "Create user")
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.createUser(request));
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return Result.success();
    }

    @Operation(summary = "Reset user password")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return Result.success();
    }

    @Operation(summary = "Update user status")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
