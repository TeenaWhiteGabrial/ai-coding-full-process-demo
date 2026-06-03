package com.aistudio.service.service;

import com.aistudio.service.dto.request.ChangePasswordRequest;
import com.aistudio.service.dto.request.ResetPasswordRequest;
import com.aistudio.service.dto.request.UpdateProfileRequest;
import com.aistudio.service.dto.request.UserCreateRequest;
import com.aistudio.service.dto.request.UserUpdateRequest;
import com.aistudio.service.dto.response.PageResult;
import com.aistudio.service.dto.response.UserManageVO;

public interface UserService {

    PageResult<UserManageVO> listUsers(int page, int size, String keyword, Long orgId);

    Long createUser(UserCreateRequest request);

    void updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    void resetPassword(Long id, ResetPasswordRequest request);

    void updateStatus(Long id, Integer status);

    void changePassword(Long userId, ChangePasswordRequest request);

    void updateProfile(Long userId, UpdateProfileRequest request);
}
