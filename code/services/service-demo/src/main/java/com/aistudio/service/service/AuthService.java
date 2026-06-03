package com.aistudio.service.service;

import com.aistudio.service.dto.request.LoginRequest;
import com.aistudio.service.dto.response.CaptchaResponse;
import com.aistudio.service.dto.response.LoginResponse;
import com.aistudio.service.dto.response.TokenResponse;
import com.aistudio.service.dto.response.UserInfoResponse;

public interface AuthService {

    CaptchaResponse getCaptcha();

    LoginResponse login(LoginRequest request);

    TokenResponse getToken(LoginRequest request);

    UserInfoResponse getUserInfo(Long userId);
}
