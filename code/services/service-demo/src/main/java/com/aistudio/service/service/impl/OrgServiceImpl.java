package com.aistudio.service.service.impl;

import com.aistudio.service.common.exception.BusinessException;
import com.aistudio.service.dto.request.OrgCreateRequest;
import com.aistudio.service.dto.response.OrgOptionVO;
import com.aistudio.service.dto.response.OrgTreeVO;
import com.aistudio.service.entity.SysOrg;
import com.aistudio.service.entity.SysUser;
import com.aistudio.service.mapper.SysOrgMapper;
import com.aistudio.service.mapper.SysUserMapper;
import com.aistudio.service.service.OrgService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService {

    private final SysOrgMapper orgMapper;
    private final SysUserMapper userMapper;

    @Override
    public List<OrgTreeVO> listTree() {
        List<SysOrg> orgs = listAll();
        return buildTree(orgs, 0L);
    }

    @Override
    public List<OrgOptionVO> listOptions() {
        List<SysOrg> orgs = listAll();
        return buildOptions(orgs, 0L);
    }

    @Override
    public SysOrg getById(Long id) {
        return requireOrg(id);
    }

    @Override
    @Transactional
    public Long create(OrgCreateRequest request) {
        validateParent(request.getParentId(), null);
        ensureUniqueCode(request.getOrgCode(), null);

        SysOrg org = new SysOrg();
        fillOrg(org, request);
        orgMapper.insert(org);
        return org.getId();
    }

    @Override
    @Transactional
    public void update(Long id, OrgCreateRequest request) {
        SysOrg org = requireOrg(id);
        validateParent(request.getParentId(), id);
        ensureUniqueCode(request.getOrgCode(), id);

        fillOrg(org, request);
        orgMapper.updateById(org);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireOrg(id);
        long childCount = orgMapper.selectCount(new LambdaQueryWrapper<SysOrg>().eq(SysOrg::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("当前组织存在子组织，不能删除");
        }
        long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getOrgId, id));
        if (userCount > 0) {
            throw new BusinessException("当前组织下存在用户，不能删除");
        }
        orgMapper.deleteById(id);
    }

    @Override
    public SysOrg requireOrg(Long id) {
        SysOrg org = orgMapper.selectById(id);
        if (org == null) {
            throw new BusinessException(404, "组织不存在");
        }
        return org;
    }

    private List<SysOrg> listAll() {
        return orgMapper.selectList(new LambdaQueryWrapper<SysOrg>()
                .orderByAsc(SysOrg::getParentId)
                .orderByAsc(SysOrg::getId));
    }

    private void fillOrg(SysOrg org, OrgCreateRequest request) {
        org.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        org.setOrgName(normalize(request.getOrgName()));
        org.setOrgCode(normalize(request.getOrgCode()));
        org.setLeaderName(normalize(request.getLeaderName()));
        org.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    }

    private void ensureUniqueCode(String orgCode, Long excludeId) {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getOrgCode, normalize(orgCode));
        if (excludeId != null) {
            wrapper.ne(SysOrg::getId, excludeId);
        }
        long count = orgMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("组织编码已存在");
        }
    }

    private void validateParent(Long parentId, Long currentId) {
        Long effectiveParentId = parentId == null ? 0L : parentId;
        if (effectiveParentId == 0L) {
            return;
        }
        if (currentId != null && currentId.equals(effectiveParentId)) {
            throw new BusinessException("上级组织不能选择自己");
        }
        SysOrg parent = requireOrg(effectiveParentId);
        if (currentId != null && isDescendant(parent.getId(), currentId)) {
            throw new BusinessException("上级组织不能选择当前组织的子级");
        }
    }

    private boolean isDescendant(Long candidateParentId, Long currentId) {
        Long cursor = candidateParentId;
        while (cursor != null && cursor != 0L) {
            if (cursor.equals(currentId)) {
                return true;
            }
            SysOrg org = orgMapper.selectById(cursor);
            cursor = org == null ? 0L : org.getParentId();
        }
        return false;
    }

    private List<OrgTreeVO> buildTree(List<SysOrg> orgs, Long parentId) {
        List<OrgTreeVO> result = new ArrayList<>();
        for (SysOrg org : orgs) {
            if (!parentId.equals(org.getParentId())) {
                continue;
            }
            OrgTreeVO vo = new OrgTreeVO();
            BeanUtils.copyProperties(org, vo);
            vo.setChildren(buildTree(orgs, org.getId()));
            result.add(vo);
        }
        return result;
    }

    private List<OrgOptionVO> buildOptions(List<SysOrg> orgs, Long parentId) {
        List<OrgOptionVO> result = new ArrayList<>();
        for (SysOrg org : orgs) {
            if (!parentId.equals(org.getParentId())) {
                continue;
            }
            OrgOptionVO vo = new OrgOptionVO();
            BeanUtils.copyProperties(org, vo);
            vo.setChildren(buildOptions(orgs, org.getId()));
            result.add(vo);
        }
        return result;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
