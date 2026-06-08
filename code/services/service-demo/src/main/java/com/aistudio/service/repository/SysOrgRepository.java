package com.aistudio.service.repository;

import com.aistudio.service.entity.SysOrg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysOrgRepository extends JpaRepository<SysOrg, Long> {

    List<SysOrg> findAllByOrderByParentIdAscIdAsc();

    List<SysOrg> findByStatusOrderByIdAsc(Integer status);

    long countByParentId(Long parentId);

    long countByOrgCode(String orgCode);
}
