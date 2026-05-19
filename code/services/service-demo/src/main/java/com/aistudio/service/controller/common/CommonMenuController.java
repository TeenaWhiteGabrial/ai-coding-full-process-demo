package com.aistudio.service.controller.common;

import com.aistudio.service.common.Result;
import com.aistudio.service.dto.request.MenuRequest;
import com.aistudio.service.entity.SysMenu;
import com.aistudio.service.service.MenuService;
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

@Tag(name = "Common Menu Management")
@RestController
@RequestMapping("/common/menu")
@RequiredArgsConstructor
public class CommonMenuController {

    private final MenuService menuService;

    @Operation(summary = "Menu list")
    @GetMapping("/list")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<SysMenu>> list() {
        return Result.success(menuService.listAllMenus());
    }

    @Operation(summary = "Menu detail")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<SysMenu> detail(@PathVariable Long id) {
        return Result.success(menuService.getMenuById(id));
    }

    @Operation(summary = "Create menu")
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Long> create(@Valid @RequestBody MenuRequest request) {
        return Result.success(menuService.createMenu(request));
    }

    @Operation(summary = "Update menu")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MenuRequest request) {
        menuService.updateMenu(id, request);
        return Result.success();
    }

    @Operation(summary = "Delete menu")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }
}
