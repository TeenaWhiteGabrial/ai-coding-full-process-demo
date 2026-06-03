package com.aistudio.service.controller.common;

import com.aistudio.service.common.Result;
import com.aistudio.service.common.SecurityUtils;
import com.aistudio.service.config.RsaConfig;
import com.aistudio.service.dto.request.ChangePasswordRequest;
import com.aistudio.service.dto.request.LoginRequest;
import com.aistudio.service.dto.request.UpdateProfileRequest;
import com.aistudio.service.dto.response.CaptchaResponse;
import com.aistudio.service.dto.response.LoginResponse;
import com.aistudio.service.dto.response.TokenResponse;
import com.aistudio.service.dto.response.UserInfoResponse;
import com.aistudio.service.service.AuthService;
import com.aistudio.service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/common/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RsaConfig rsaConfig;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Get RSA public key")
    @GetMapping("/public-key")
    public Result<String> publicKey() {
        return Result.success(rsaConfig.getPublicKeyBase64());
    }

    @Operation(summary = "Get captcha")
    @GetMapping("/captcha")
    public Result<CaptchaResponse> captcha() {
        return Result.success(authService.getCaptcha());
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "Get token")
    @PostMapping("/token")
    public Result<TokenResponse> token(@Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest request) {
        return Result.success(authService.getToken(request));
    }

    @Operation(summary = "Get current user")
    @GetMapping("/user-info")
    public Result<UserInfoResponse> userInfo() {
        Long userId = securityUtils.getCurrentUserId();
        return Result.success(authService.getUserInfo(userId));
    }

    @Operation(summary = "Change password")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @org.springframework.web.bind.annotation.RequestBody ChangePasswordRequest request) {
        userService.changePassword(securityUtils.getCurrentUserId(), request);
        return Result.success();
    }

    @Operation(summary = "Update profile")
    @PostMapping("/update-profile")
    public Result<Void> updateProfile(@Valid @org.springframework.web.bind.annotation.RequestBody UpdateProfileRequest request) {
        userService.updateProfile(securityUtils.getCurrentUserId(), request);
        return Result.success();
    }
}
