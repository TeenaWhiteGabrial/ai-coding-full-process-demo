package com.aistudio.service.service;

import com.aistudio.service.dto.request.OrgCreateRequest;
import com.aistudio.service.dto.response.OrgOptionVO;
import com.aistudio.service.dto.response.OrgTreeVO;
import com.aistudio.service.entity.SysOrg;

import java.util.List;

public interface OrgService {

    List<OrgTreeVO> listTree();

    List<OrgOptionVO> listOptions();

    SysOrg getById(Long id);

    Long create(OrgCreateRequest request);

    void update(Long id, OrgCreateRequest request);

    void delete(Long id);

    SysOrg requireOrg(Long id);
}
