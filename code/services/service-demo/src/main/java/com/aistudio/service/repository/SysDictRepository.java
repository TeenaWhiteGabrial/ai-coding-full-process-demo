package com.aistudio.service.repository;

import com.aistudio.service.entity.SysDict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysDictRepository extends JpaRepository<SysDict, Long> {

    List<SysDict> findByDictTypeOrderBySortAscIdAsc(String dictType);

    List<SysDict> findAllByOrderBySortAscIdAsc();

    long countByParentId(Long parentId);

    long countByDictTypeAndDictValue(String dictType, String dictValue);

    long countByDictTypeAndDictValueAndIdNot(String dictType, String dictValue, Long id);
}
