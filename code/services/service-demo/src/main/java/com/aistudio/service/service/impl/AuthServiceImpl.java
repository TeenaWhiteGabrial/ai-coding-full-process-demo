package com.aistudio.service.service.impl;

import com.aistudio.service.common.exception.BusinessException;
import com.aistudio.service.config.JwtTokenProvider;
import com.aistudio.service.config.RsaConfig;
import com.aistudio.service.dto.request.LoginRequest;
import com.aistudio.service.dto.response.CaptchaResponse;
import com.aistudio.service.dto.response.LoginResponse;
import com.aistudio.service.dto.response.TokenResponse;
import com.aistudio.service.dto.response.UserInfoResponse;
import com.aistudio.service.entity.SysRole;
import com.aistudio.service.entity.SysUser;
import com.aistudio.service.mapper.SysMenuMapper;
import com.aistudio.service.mapper.SysRoleMapper;
import com.aistudio.service.mapper.SysUserMapper;
import com.aistudio.service.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    private static final int LOGIN_FAIL_LIMIT = 5;
    private static final int LOGIN_LOCK_MINUTES = 10;
    private static final long CAPTCHA_EXPIRE_SECONDS = 300;

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RsaConfig rsaConfig;

    private final Map<String, CaptchaHolder> captchaStore = new ConcurrentHashMap<>();

    @Override
    public CaptchaResponse getCaptcha() {
        cleanupCaptchaStore();
        String captchaCode = randomCaptchaCode();
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        captchaStore.put(captchaKey, new CaptchaHolder(captchaCode, LocalDateTime.now().plusSeconds(CAPTCHA_EXPIRE_SECONDS)));
        return CaptchaResponse.builder()
                .captchaKey(captchaKey)
                .captchaImage(renderCaptchaImage(captchaCode))
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String plainPassword = rsaConfig.decrypt(request.getPassword());
        SysUser user = validateLogin(request, plainPassword);
        List<SysRole> roleEntities = roleMapper.selectByUserId(user.getId());
        List<String> roles = roleEntities.stream().map(SysRole::getRoleCode).toList();
        Set<String> permissions = resolvePermissions(user.getId(), roleEntities);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .permissions(permissions.stream().sorted().toList())
                .build();
    }

    @Override
    public TokenResponse getToken(LoginRequest request) {
        String plainPassword = rsaConfig.decrypt(request.getPassword());
        SysUser user = validateLogin(request, plainPassword);
        return TokenResponse.builder()
                .token(jwtTokenProvider.generateToken(user.getId(), user.getUsername()))
                .build();
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        List<SysRole> roles = roleMapper.selectByUserId(userId);
        Set<String> permissions = resolvePermissions(userId, roles);
        return UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles.stream().map(SysRole::getRoleCode).toList())
                .permissions(permissions.stream().sorted().toList())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .build();
    }

    private Set<String> resolvePermissions(Long userId, List<SysRole> roles) {
        boolean isSuperAdmin = roles.stream()
                .anyMatch(role -> ROLE_SUPER_ADMIN.equals(role.getRoleCode()));
        if (isSuperAdmin) {
            return menuMapper.selectAllConsoleMenus().stream()
                    .map(com.aistudio.service.entity.SysMenu::getPermissionCode)
                    .filter(code -> code != null && !code.isBlank())
                    .collect(java.util.stream.Collectors.toSet());
        }
        return Set.copyOf(menuMapper.selectPermissionCodesByUserId(userId));
    }

    private SysUser validateLogin(LoginRequest request, String plainPassword) {
        validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户名、密码或验证码错误");
        }
        if (isLocked(user)) {
            long minutes = LocalDateTime.now().until(user.getLockedUntil(), ChronoUnit.MINUTES) + 1;
            throw new BusinessException(423, "登录失败次数过多，请在 " + Math.max(minutes, 1) + " 分钟后重试");
        }
        if (!matchesPassword(plainPassword, user.getPassword())) {
            handleLoginFailure(user);
            throw new BusinessException(401, "用户名、密码或验证码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "用户已被禁用");
        }
        resetLoginFailure(user);
        return user;
    }

    private void validateCaptcha(String captchaKey, String captchaCode) {
        cleanupCaptchaStore();
        CaptchaHolder holder = captchaStore.remove(captchaKey);
        if (holder == null || holder.expireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "验证码已失效，请刷新后重试");
        }
        if (!holder.code().equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException(400, "验证码错误");
        }
    }

    private void handleLoginFailure(SysUser user) {
        int failedCount = (user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount()) + 1;
        user.setFailedLoginCount(failedCount);
        if (failedCount >= LOGIN_FAIL_LIMIT) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOGIN_LOCK_MINUTES));
        }
        userMapper.updateById(user);
    }

    private void resetLoginFailure(SysUser user) {
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userMapper.updateById(user);
    }

    private boolean isLocked(SysUser user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        return rawPassword != null
                && storedPassword != null
                && (storedPassword.equals(rawPassword) || passwordEncoder.matches(rawPassword, storedPassword));
    }

    private String randomCaptchaCode() {
        String seed = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            builder.append(seed.charAt((int) (Math.random() * seed.length())));
        }
        return builder.toString();
    }

    private String renderCaptchaImage(String code) {
        try {
            BufferedImage image = new BufferedImage(132, 44, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(new Color(245, 247, 250));
            graphics.fillRect(0, 0, 132, 44);
            graphics.setFont(new Font("SansSerif", Font.BOLD, 24));
            graphics.setColor(new Color(31, 41, 55));
            graphics.drawString(code, 24, 30);
            graphics.setColor(new Color(148, 163, 184));
            for (int i = 0; i < 8; i++) {
                int x1 = (int) (Math.random() * 132);
                int y1 = (int) (Math.random() * 44);
                int x2 = (int) (Math.random() * 132);
                int y2 = (int) (Math.random() * 44);
                graphics.drawLine(x1, y1, x2, y2);
            }
            graphics.dispose();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception exception) {
            throw new BusinessException(500, "验证码生成失败");
        }
    }

    private void cleanupCaptchaStore() {
        LocalDateTime now = LocalDateTime.now();
        captchaStore.entrySet().removeIf(entry -> entry.getValue().expireAt().isBefore(now));
    }

    private record CaptchaHolder(String code, LocalDateTime expireAt) {
    }
}
