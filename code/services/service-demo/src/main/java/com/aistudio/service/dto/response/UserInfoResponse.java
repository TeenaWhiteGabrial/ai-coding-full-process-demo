package com.aistudio.service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserInfoResponse {

    private Long userId;
    private String username;
    private String realName;
    private List<String> roles;
    private List<String> permissions;
    private String avatar;
    private String email;
}
