package com.aistudio.service.controller.common;

import com.aistudio.service.common.Result;
import com.aistudio.service.common.exception.BusinessException;
import com.aistudio.service.dto.request.RoleCreateRequest;
import com.aistudio.service.dto.response.RoleMenuTreeVO;
import com.aistudio.service.entity.SysRole;
import com.aistudio.service.entity.SysUserRole;
import com.aistudio.service.repository.SysRoleRepository;
import com.aistudio.service.repository.SysUserRoleRepository;
import com.aistudio.service.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Tag(name = "Common Role Management")
@RestController
@RequestMapping("/common/role")
@RequiredArgsConstructor
public class CommonRoleController {

    private final SysRoleRepository roleRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final MenuService menuService;

    @Operation(summary = "Role list")
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OP_ADMIN')")
    public Result<List<SysRole>> list() {
        return Result.success(roleRepository.findAll());
    }

    @Operation(summary = "Create role")
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Long> create(@jakarta.validation.Valid @RequestBody RoleCreateRequest request) {
        long count = roleRepository.countByRoleCode(request.getRoleCode());
        if (count > 0) {
            throw new BusinessException(400, "角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode().trim());
        role.setRoleName(request.getRoleName().trim());
        roleRepository.save(role);
        return Result.success(role.getId());
    }

    @Operation(summary = "Role menu tree")
    @GetMapping("/{id}/menus")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<RoleMenuTreeVO>> roleMenus(@PathVariable Long id) {
        ensureEditableRole(id);
        return Result.success(menuService.getRoleMenuTree(id));
    }

    @Operation(summary = "Update role menus")
    @PutMapping("/{id}/menus")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateRoleMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        ensureEditableRole(id);
        menuService.updateRoleMenus(id, menuIds == null ? List.of() : menuIds);
        return Result.success();
    }

    @Operation(summary = "Role users")
    @GetMapping("/{id}/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<Long>> roleUsers(@PathVariable Long id) {
        ensureEditableRole(id);
        List<Long> userIds = userRoleRepository.findByRoleId(id)
                .stream()
                .map(SysUserRole::getUserId)
                .toList();
        return Result.success(userIds);
    }

    @Operation(summary = "Update role users")
    @PutMapping("/{id}/users")
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateRoleUsers(@PathVariable Long id, @RequestBody List<Long> userIds) {
        ensureEditableRole(id);
        userRoleRepository.deleteByRoleId(id);
        for (Long userId : Optional.ofNullable(userIds).orElse(List.of()).stream().distinct().toList()) {
            SysUserRole relation = new SysUserRole();
            relation.setRoleId(id);
            relation.setUserId(userId);
            userRoleRepository.save(relation);
        }
        return Result.success();
    }

    @Operation(summary = "Delete role")
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        ensureEditableRole(id);
        long userCount = userRoleRepository.countByRoleId(id);
        if (userCount > 0) {
            throw new BusinessException(400, "该角色下仍有关联用户，不能删除");
        }
        userRoleRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
        return Result.success();
    }

    private void ensureEditableRole(Long id) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "角色不存在"));
        if ("SUPER_ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException(403, "超级管理员角色不允许修改");
        }
    }
}
