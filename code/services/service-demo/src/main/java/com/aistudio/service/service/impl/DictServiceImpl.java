package com.aistudio.service.service.impl;

import com.aistudio.service.common.exception.BusinessException;
import com.aistudio.service.dto.request.DictCreateRequest;
import com.aistudio.service.dto.request.DictUpdateRequest;
import com.aistudio.service.dto.response.DictTreeVO;
import com.aistudio.service.entity.SysDict;
import com.aistudio.service.mapper.SysDictMapper;
import com.aistudio.service.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    private final SysDictMapper dictMapper;

    @Override
    public List<DictTreeVO> getDictTree(String dictType) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
                .orderByAsc(SysDict::getSort)
                .orderByAsc(SysDict::getId);
        if (StringUtils.hasText(dictType)) {
            wrapper.eq(SysDict::getDictType, dictType.trim());
        }
        List<SysDict> dicts = dictMapper.selectList(wrapper);
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
        dictMapper.insert(dict);
        return dict.getId();
    }

    @Override
    @Transactional
    public void updateDict(Long id, DictUpdateRequest request) {
        SysDict dict = requireDict(id);
        ensureUnique(request.getDictType(), request.getDictValue(), id);
        applyRequest(dict, request);
        dict.setId(id);
        dictMapper.updateById(dict);
    }

    @Override
    @Transactional
    public void deleteDict(Long id) {
        long childCount = dictMapper.selectCount(new LambdaQueryWrapper<SysDict>().eq(SysDict::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(400, "请先删除子级字典项");
        }
        dictMapper.deleteById(id);
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
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, dictType.trim())
                .eq(SysDict::getDictValue, dictValue.trim());
        if (excludeId != null) {
            wrapper.ne(SysDict::getId, excludeId);
        }
        if (dictMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "同类型字典值已存在");
        }
    }

    private SysDict requireDict(Long id) {
        SysDict dict = dictMapper.selectById(id);
        if (dict == null) {
            throw new BusinessException(404, "字典项不存在");
        }
        return dict;
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
