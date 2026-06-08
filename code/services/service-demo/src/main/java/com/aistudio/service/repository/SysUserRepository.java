package com.aistudio.service.repository;

import com.aistudio.service.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

    Optional<SysUser> findByUsername(String username);

    long countByUsername(String username);

    long countByOrgId(Long orgId);

    Page<SysUser> findAllByOrderByIdDesc(Pageable pageable);
}
