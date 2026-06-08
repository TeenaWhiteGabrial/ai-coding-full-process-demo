package com.aistudio.service.repository;

import com.aistudio.service.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {

    List<SysMenu> findByAppCodeOrderBySortAscIdAsc(String appCode);

    long countByParentId(Long parentId);

    @Query("""
            select distinct m from SysMenu m
            join SysRoleMenu rm on rm.menuId = m.id
            join SysUserRole ur on ur.roleId = rm.roleId
            where ur.userId = :userId
              and m.appCode = 'CONSOLE'
              and m.hidden = 0
            order by m.sort asc, m.id asc
            """)
    List<SysMenu> findConsoleMenusByUserId(@Param("userId") Long userId);

    @Query("""
            select m from SysMenu m
            where m.appCode = 'CONSOLE'
              and m.hidden = 0
            order by m.sort asc, m.id asc
            """)
    List<SysMenu> findAllVisibleConsoleMenus();

    @Query("""
            select distinct m.permissionCode from SysMenu m
            join SysRoleMenu rm on rm.menuId = m.id
            join SysUserRole ur on ur.roleId = rm.roleId
            where ur.userId = :userId
              and m.permissionCode is not null
              and m.permissionCode <> ''
            """)
    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);
}
