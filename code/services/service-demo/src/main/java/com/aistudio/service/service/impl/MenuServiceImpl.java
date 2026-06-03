package com.aistudio.service.service.impl;

import com.aistudio.service.common.exception.BusinessException;
import com.aistudio.service.dto.request.MenuRequest;
import com.aistudio.service.dto.response.MenuTreeVO;
import com.aistudio.service.dto.response.RoleMenuTreeVO;
import com.aistudio.service.entity.SysMenu;
import com.aistudio.service.entity.SysRoleMenu;
import com.aistudio.service.mapper.SysMenuMapper;
import com.aistudio.service.mapper.SysRoleMenuMapper;
import com.aistudio.service.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private static final String APP_CODE_CONSOLE = "CONSOLE";
    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    private final SysMenuMapper menuMapper;
    private final com.aistudio.service.mapper.SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<MenuTreeVO> getMenuTree(Long userId) {
        boolean isSuperAdmin = roleMapper.selectByUserId(userId).stream()
                .anyMatch(role -> ROLE_SUPER_ADMIN.equals(role.getRoleCode()));
        List<SysMenu> menus = isSuperAdmin
                ? menuMapper.selectAllConsoleMenus()
                : menuMapper.selectByUserId(userId);
        menus = menus.stream()
                .filter(menu -> !"BUTTON".equalsIgnoreCase(menu.getMenuType()))
                .toList();
        List<MenuTreeVO> voList = new ArrayList<>();
        for (SysMenu menu : menus) {
            MenuTreeVO vo = new MenuTreeVO();
            BeanUtils.copyProperties(menu, vo);
            voList.add(vo);
        }
        return buildTree(voList, 0L);
    }

    @Override
    public List<RoleMenuTreeVO> getRoleMenuTree(Long roleId) {
        List<SysMenu> allMenus = menuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getAppCode, APP_CODE_CONSOLE)
                        .orderByAsc(SysMenu::getSort)
                        .orderByAsc(SysMenu::getId));
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId));
        Set<Long> roleMenuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());

        List<RoleMenuTreeVO> voList = allMenus.stream().map(menu -> {
            RoleMenuTreeVO vo = new RoleMenuTreeVO();
            BeanUtils.copyProperties(menu, vo);
            vo.setChecked(roleMenuIds.contains(menu.getId()));
            return vo;
        }).toList();
        return buildRoleTree(voList, 0L);
    }

    @Override
    @Transactional
    public void updateRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
    }

    @Override
    public List<SysMenu> listAllMenus() {
        return menuMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getAppCode, APP_CODE_CONSOLE)
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));
    }

    @Override
    public SysMenu getMenuById(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(404, "菜单不存在");
        }
        return menu;
    }

    @Override
    public Long createMenu(MenuRequest request) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(request, menu);
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        menu.setMenuType(request.getMenuType() == null ? "MENU" : request.getMenuType());
        menu.setPermissionCode(blankToNull(request.getPermissionCode()));
        menu.setSort(request.getSort() == null ? 0 : request.getSort());
        menu.setHidden(request.getHidden() == null ? 0 : request.getHidden());
        menu.setAppCode(APP_CODE_CONSOLE);
        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    public void updateMenu(Long id, MenuRequest request) {
        SysMenu menu = getMenuById(id);
        BeanUtils.copyProperties(request, menu);
        menu.setId(id);
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        menu.setMenuType(request.getMenuType() == null ? "MENU" : request.getMenuType());
        menu.setPermissionCode(blankToNull(request.getPermissionCode()));
        menu.setSort(request.getSort() == null ? 0 : request.getSort());
        menu.setHidden(request.getHidden() == null ? 0 : request.getHidden());
        menu.setAppCode(APP_CODE_CONSOLE);
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        long childCount = menuMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(400, "请先删除子菜单");
        }
        List<Long> assignedRoleIds = roleMenuMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRoleMenu>()
                                .eq(SysRoleMenu::getMenuId, id))
                .stream()
                .map(SysRoleMenu::getRoleId)
                .distinct()
                .toList();
        long roleCount = assignedRoleIds.isEmpty()
                ? 0
                : roleMapper.selectBatchIds(assignedRoleIds).stream()
                        .filter(java.util.Objects::nonNull)
                        .filter(role -> !ROLE_SUPER_ADMIN.equals(role.getRoleCode()))
                        .count();
        if (roleCount > 0) {
            throw new BusinessException(400, "菜单已分配给角色，无法删除");
        }
        menuMapper.deleteById(id);
    }

    @Override
    public Set<String> getPermissionCodes(Long userId) {
        boolean isSuperAdmin = roleMapper.selectByUserId(userId).stream()
                .anyMatch(role -> ROLE_SUPER_ADMIN.equals(role.getRoleCode()));
        if (isSuperAdmin) {
            return menuMapper.selectAllConsoleMenus().stream()
                    .map(SysMenu::getPermissionCode)
                    .filter(code -> code != null && !code.isBlank())
                    .collect(Collectors.toSet());
        }
        return menuMapper.selectPermissionCodesByUserId(userId).stream().collect(Collectors.toSet());
    }

    private List<MenuTreeVO> buildTree(List<MenuTreeVO> items, Long parentId) {
        List<MenuTreeVO> result = new ArrayList<>();
        for (MenuTreeVO item : items) {
            if (parentId.equals(item.getParentId())) {
                item.setChildren(buildTree(items, item.getId()));
                result.add(item);
            }
        }
        return result;
    }

    private List<RoleMenuTreeVO> buildRoleTree(List<RoleMenuTreeVO> items, Long parentId) {
        List<RoleMenuTreeVO> result = new ArrayList<>();
        for (RoleMenuTreeVO item : items) {
            if (parentId.equals(item.getParentId())) {
                item.setChildren(buildRoleTree(items, item.getId()));
                result.add(item);
            }
        }
        return result;
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
