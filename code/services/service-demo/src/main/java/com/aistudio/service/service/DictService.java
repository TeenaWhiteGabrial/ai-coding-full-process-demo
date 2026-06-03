package com.aistudio.service.service;

import com.aistudio.service.dto.request.DictCreateRequest;
import com.aistudio.service.dto.request.DictUpdateRequest;
import com.aistudio.service.dto.response.DictTreeVO;

import java.util.List;

public interface DictService {

    List<DictTreeVO> getDictTree(String dictType);

    DictTreeVO getDictById(Long id);

    Long createDict(DictCreateRequest request);

    void updateDict(Long id, DictUpdateRequest request);

    void deleteDict(Long id);
}
