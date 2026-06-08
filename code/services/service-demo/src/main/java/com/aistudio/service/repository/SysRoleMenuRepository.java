package com.aistudio.service.repository;

import com.aistudio.service.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Long> {

    List<SysRoleMenu> findByRoleId(Long roleId);

    List<SysRoleMenu> findByMenuId(Long menuId);

    void deleteByRoleId(Long roleId);
}
