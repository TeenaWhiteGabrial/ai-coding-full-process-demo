package com.aistudio.service.service.impl;

import com.aistudio.service.common.exception.BusinessException;
import com.aistudio.service.dto.request.DictCreateRequest;
import com.aistudio.service.dto.request.DictUpdateRequest;
import com.aistudio.service.dto.response.DictTreeVO;
import com.aistudio.service.entity.SysDict;
import com.aistudio.service.repository.SysDictRepository;
import com.aistudio.service.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictRepository dictRepository;

    @Override
    public List<DictTreeVO> getDictTree(String dictType) {
        List<SysDict> dicts = StringUtils.hasText(dictType)
                ? dictRepository.findByDictTypeOrderBySortAscIdAsc(dictType.trim())
                : dictRepository.findAllByOrderBySortAscIdAsc();
        List<DictTreeVO> items = dicts.stream().map(this::toVO).toList();
        return buildTree(items, 0L);
    }

    @Override
    public DictTreeVO getDictById(Long id) {
        return toVO(requireDict(id));
    }

    @Override
    @Transactional
    public Long createDict(DictCreateRequest request) {
        ensureUnique(request.getDictType(), request.getDictValue(), null);
        SysDict dict = new SysDict();
        applyRequest(dict, request);
        dictRepository.save(dict);
        return dict.getId();
    }

    @Override
    @Transactional
    public void updateDict(Long id, DictUpdateRequest request) {
        SysDict dict = requireDict(id);
        ensureUnique(request.getDictType(), request.getDictValue(), id);
        applyRequest(dict, request);
        dict.setId(id);
        dictRepository.save(dict);
    }

    @Override
    @Transactional
    public void deleteDict(Long id) {
        long childCount = dictRepository.countByParentId(id);
        if (childCount > 0) {
            throw new BusinessException(400, "请先删除子级字典项");
        }
        dictRepository.deleteById(id);
    }

    private void applyRequest(SysDict dict, DictCreateRequest request) {
        dict.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        dict.setDictType(request.getDictType().trim());
        dict.setDictLabel(request.getDictLabel().trim());
        dict.setDictValue(request.getDictValue().trim());
        dict.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        dict.setSort(request.getSort() == null ? 0 : request.getSort());
        dict.setRemark(normalize(request.getRemark()));
    }

    private void applyRequest(SysDict dict, DictUpdateRequest request) {
        dict.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        dict.setDictType(request.getDictType().trim());
        dict.setDictLabel(request.getDictLabel().trim());
        dict.setDictValue(request.getDictValue().trim());
        dict.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        dict.setSort(request.getSort() == null ? 0 : request.getSort());
        dict.setRemark(normalize(request.getRemark()));
    }

    private void ensureUnique(String dictType, String dictValue, Long excludeId) {
        long count = excludeId == null
                ? dictRepository.countByDictTypeAndDictValue(dictType.trim(), dictValue.trim())
                : dictRepository.countByDictTypeAndDictValueAndIdNot(dictType.trim(), dictValue.trim(), excludeId);
        if (count > 0) {
            throw new BusinessException(400, "同类型字典值已存在");
        }
    }

    private SysDict requireDict(Long id) {
        return dictRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "字典项不存在"));
    }

    private DictTreeVO toVO(SysDict dict) {
        DictTreeVO vo = new DictTreeVO();
        BeanUtils.copyProperties(dict, vo);
        return vo;
    }

    private List<DictTreeVO> buildTree(List<DictTreeVO> items, Long parentId) {
        List<DictTreeVO> result = new ArrayList<>();
        for (DictTreeVO item : items) {
            if (parentId.equals(item.getParentId())) {
                item.setChildren(buildTree(items, item.getId()));
                result.add(item);
            }
        }
        return result;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
