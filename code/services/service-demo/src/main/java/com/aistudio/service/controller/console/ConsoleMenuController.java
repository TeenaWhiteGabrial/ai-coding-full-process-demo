package com.aistudio.service.controller.console;

import com.aistudio.service.common.Result;
import com.aistudio.service.common.SecurityUtils;
import com.aistudio.service.dto.response.MenuTreeVO;
import com.aistudio.service.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Console Menu")
@RestController
@RequestMapping("/common/menu")
@RequiredArgsConstructor
public class ConsoleMenuController {

    private final MenuService menuService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Get current user menu tree")
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> tree() {
        return Result.success(menuService.getMenuTree(securityUtils.getCurrentUserId()));
    }
}
