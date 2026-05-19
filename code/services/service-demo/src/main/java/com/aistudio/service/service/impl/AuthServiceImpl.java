package com.aistudio.service.service.impl;

import com.aistudio.service.common.exception.BusinessException;
import com.aistudio.service.config.JwtTokenProvider;
import com.aistudio.service.config.RsaConfig;
import com.aistudio.service.dto.request.LoginRequest;
import com.aistudio.service.dto.response.LoginResponse;
import com.aistudio.service.dto.response.TokenResponse;
import com.aistudio.service.dto.response.UserInfoResponse;
import com.aistudio.service.entity.SysRole;
import com.aistudio.service.entity.SysUser;
import com.aistudio.service.mapper.SysRoleMapper;
import com.aistudio.service.mapper.SysUserMapper;
import com.aistudio.service.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RsaConfig rsaConfig;

    @Override
    public LoginResponse login(LoginRequest request) {
        String plainPassword = rsaConfig.decrypt(request.getPassword());
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !matchesPassword(plainPassword, user.getPassword())) {
            throw new BusinessException(401, "username or password is incorrect");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "user is disabled");
        }

        List<String> roles = roleMapper.selectByUserId(user.getId()).stream()
                .map(SysRole::getRoleCode)
                .toList();

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .build();
    }

    @Override
    public TokenResponse getToken(LoginRequest request) {
        String plainPassword = rsaConfig.decrypt(request.getPassword());
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !matchesPassword(plainPassword, user.getPassword())) {
            throw new BusinessException(401, "username or password is incorrect");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "user is disabled");
        }
        return TokenResponse.builder()
                .token(jwtTokenProvider.generateToken(user.getId(), user.getUsername()))
                .build();
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        List<String> roles = roleMapper.selectByUserId(userId).stream()
                .map(SysRole::getRoleCode)
                .toList();
        return UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .build();
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        return rawPassword != null
                && storedPassword != null
                && (storedPassword.equals(rawPassword) || passwordEncoder.matches(rawPassword, storedPassword));
    }
}
