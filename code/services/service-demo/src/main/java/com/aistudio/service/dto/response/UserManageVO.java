package com.aistudio.service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserManageVO {

    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Long orgId;
    private String orgName;
    private Integer status;
    private Integer failedLoginCount;
    private String lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> roleIds;
    private List<String> roleCodes;
    private List<String> roleNames;
}
