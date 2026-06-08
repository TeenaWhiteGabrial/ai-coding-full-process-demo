package com.aistudio.service.repository;

import com.aistudio.service.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    Optional<SysRole> findByRoleCode(String roleCode);

    long countByRoleCode(String roleCode);

    @Query("""
            select r from SysRole r
            join SysUserRole ur on ur.roleId = r.id
            where ur.userId = :userId
            order by r.id asc
            """)
    List<SysRole> findByUserId(@Param("userId") Long userId);
}
