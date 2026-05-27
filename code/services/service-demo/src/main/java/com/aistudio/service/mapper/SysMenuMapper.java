package com.aistudio.service.mapper;

import com.aistudio.service.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("""
            SELECT m.*
            FROM sys_menu m
            WHERE COALESCE(m.app_code, 'CONSOLE') = 'CONSOLE'
            ORDER BY m.sort ASC, m.id ASC
            """)
    List<SysMenu> selectAllConsoleMenus();

    @Select("""
            SELECT DISTINCT m.*
            FROM sys_menu m
            INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
            INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND COALESCE(m.app_code, 'CONSOLE') = 'CONSOLE'
            ORDER BY m.sort ASC, m.id ASC
            """)
    List<SysMenu> selectByUserId(Long userId);
}
