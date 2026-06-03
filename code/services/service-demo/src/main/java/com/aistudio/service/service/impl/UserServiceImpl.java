package com.aistudio.service.service.impl;

import com.aistudio.service.common.SecurityUtils;
import com.aistudio.service.common.exception.BusinessException;
import com.aistudio.service.dto.request.ChangePasswordRequest;
import com.aistudio.service.dto.request.ResetPasswordRequest;
import com.aistudio.service.dto.request.UpdateProfileRequest;
import com.aistudio.service.dto.request.UserCreateRequest;
import com.aistudio.service.dto.request.UserUpdateRequest;
import com.aistudio.service.dto.response.PageResult;
import com.aistudio.service.dto.response.UserManageVO;
import com.aistudio.service.entity.SysRole;
import com.aistudio.service.entity.SysOrg;
import com.aistudio.service.entity.SysUser;
import com.aistudio.service.entity.SysUserRole;
import com.aistudio.service.mapper.SysOrgMapper;
import com.aistudio.service.mapper.SysRoleMapper;
import com.aistudio.service.mapper.SysUserMapper;
import com.aistudio.service.mapper.SysUserRoleMapper;
import com.aistudio.service.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String BUILTIN_ADMIN_USERNAME = "admin";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper userMapper;
    private final SysOrgMapper orgMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    public PageResult<UserManageVO> listUsers(int page, int size, String keyword, Long orgId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(query -> query
                    .like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword)
                    .or()
                    .like(SysUser::getEmail, keyword)
                    .or()
                    .like(SysUser::getPhone, keyword));
        }
        if (orgId != null) {
            wrapper.eq(SysUser::getOrgId, orgId);
        }
        wrapper.orderByDesc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(new Page<>(page, size), wrapper);

        List<Long> userIds = result.getRecords().stream().map(SysUser::getId).toList();
        List<SysUserRole> userRoles = userIds.isEmpty() ? List.of() : userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
        Map<Long, List<Long>> roleIdsByUser = userRoles.stream().collect(
                Collectors.groupingBy(SysUserRole::getUserId,
                        Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));

        List<Long> allRoleIds = userRoles.stream().map(SysUserRole::getRoleId).distinct().toList();
        Map<Long, SysRole> roleMap = allRoleIds.isEmpty() ? Map.of() : roleMapper.selectBatchIds(allRoleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, Function.identity()));
        List<Long> orgIds = result.getRecords().stream()
                .map(SysUser::getOrgId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, SysOrg> orgMap = orgIds.isEmpty() ? Map.of() : orgMapper.selectBatchIds(orgIds).stream()
                .collect(Collectors.toMap(SysOrg::getId, Function.identity()));

        List<UserManageVO> records = result.getRecords().stream().map(user -> {
            List<Long> roleIds = roleIdsByUser.getOrDefault(user.getId(), List.of());
            List<SysRole> roles = roleIds.stream()
                    .map(roleMap::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            SysOrg org = user.getOrgId() == null ? null : orgMap.get(user.getOrgId());
            UserManageVO vo = new UserManageVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            vo.setAvatar(user.getAvatar());
            vo.setOrgId(user.getOrgId());
            vo.setOrgName(org == null ? null : org.getOrgName());
            vo.setStatus(user.getStatus());
            vo.setFailedLoginCount(user.getFailedLoginCount());
            vo.setLockedUntil(user.getLockedUntil() == null ? null : user.getLockedUntil().format(DATE_TIME_FORMATTER));
            vo.setCreatedAt(user.getCreatedAt());
            vo.setUpdatedAt(user.getUpdatedAt());
            vo.setRoleIds(roleIds);
            vo.setRoleCodes(roles.stream().map(SysRole::getRoleCode).toList());
            vo.setRoleNames(roles.stream().map(SysRole::getRoleName).toList());
            return vo;
        }).toList();
        return PageResult.of(result.getTotal(), records);
    }

    @Override
    @Transactional
    public Long createUser(UserCreateRequest request) {
        long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(normalize(request.getRealName()));
        user.setEmail(normalize(request.getEmail()));
        user.setPhone(normalize(request.getPhone()));
        user.setAvatar(normalize(request.getAvatar()));
        user.setOrgId(resolveOrgId(request.getOrgId()));
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        userMapper.insert(user);

        replaceUserRoles(user.getId(), request.getRoleIds());
        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        SysUser user = requireUser(id);
        ensureBuiltinAdminMutable(user, "admin 账号不允许编辑");
        if (request.getRealName() != null) {
            user.setRealName(normalize(request.getRealName()));
        }
        if (request.getEmail() != null) {
            user.setEmail(normalize(request.getEmail()));
        }
        if (request.getPhone() != null) {
            user.setPhone(normalize(request.getPhone()));
        }
        if (request.getAvatar() != null) {
            user.setAvatar(normalize(request.getAvatar()));
        }
        if (request.getOrgId() != null || user.getOrgId() != null) {
            user.setOrgId(resolveOrgId(request.getOrgId()));
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        userMapper.updateById(user);

        if (request.getRoleIds() != null) {
            replaceUserRoles(id, request.getRoleIds());
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = requireUser(id);
        ensureBuiltinAdminMutable(user, "admin 账号不允许删除");
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        userMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id, ResetPasswordRequest request) {
        SysUser user = requireUser(id);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userMapper.updateById(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = requireUser(id);
        ensureBuiltinAdminMutable(user, "admin 账号不允许禁用");
        user.setStatus(status == null ? 1 : status);
        if (status != null && status == 0) {
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
        }
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(userId)) {
            throw new BusinessException(403, "只能修改自己的密码");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!matchesPassword(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(userId)) {
            throw new BusinessException(403, "只能修改自己的个人资料");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        return rawPassword != null
                && storedPassword != null
                && (storedPassword.equals(rawPassword) || passwordEncoder.matches(rawPassword, storedPassword));
    }

    private SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private void ensureBuiltinAdminMutable(SysUser user, String message) {
        if (BUILTIN_ADMIN_USERNAME.equals(user.getUsername())) {
            throw new BusinessException(400, message);
        }
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<Long> effectiveRoleIds = roleIds;
        if (effectiveRoleIds == null || effectiveRoleIds.isEmpty()) {
            SysRole defaultRole = roleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "USER"));
            effectiveRoleIds = defaultRole == null ? List.of() : List.of(defaultRole.getId());
        }
        for (Long roleId : effectiveRoleIds.stream().distinct().toList()) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            userRoleMapper.insert(relation);
        }
    }

    private Long resolveOrgId(Long orgId) {
        if (orgId == null || orgId == 0L) {
            return null;
        }
        SysOrg org = orgMapper.selectById(orgId);
        if (org == null) {
            throw new BusinessException(400, "组织不存在");
        }
        return orgId;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
