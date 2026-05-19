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
import com.aistudio.service.entity.SysUser;
import com.aistudio.service.entity.SysUserRole;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    public PageResult<UserManageVO> listUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(query -> query
                    .like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword)
                    .or()
                    .like(SysUser::getEmail, keyword));
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

        List<UserManageVO> records = result.getRecords().stream().map(user -> {
            List<Long> roleIds = roleIdsByUser.getOrDefault(user.getId(), List.of());
            List<SysRole> roles = roleIds.stream()
                    .map(roleMap::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            UserManageVO vo = new UserManageVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setEmail(user.getEmail());
            vo.setAvatar(user.getAvatar());
            vo.setStatus(user.getStatus());
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
            throw new BusinessException(400, "username already exists");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(normalize(request.getRealName()));
        user.setEmail(normalize(request.getEmail()));
        user.setAvatar(normalize(request.getAvatar()));
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        userMapper.insert(user);

        replaceUserRoles(user.getId(), request.getRoleIds());
        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        SysUser user = requireUser(id);
        if (request.getRealName() != null) {
            user.setRealName(normalize(request.getRealName()));
        }
        if (request.getEmail() != null) {
            user.setEmail(normalize(request.getEmail()));
        }
        if (request.getAvatar() != null) {
            user.setAvatar(normalize(request.getAvatar()));
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
        requireUser(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        userMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id, ResetPasswordRequest request) {
        SysUser user = requireUser(id);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(userId)) {
            throw new BusinessException(403, "can only change your own password");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        if (!matchesPassword(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(userId)) {
            throw new BusinessException(403, "can only update your own profile");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "user not found");
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
            throw new BusinessException(404, "user not found");
        }
        return user;
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

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
