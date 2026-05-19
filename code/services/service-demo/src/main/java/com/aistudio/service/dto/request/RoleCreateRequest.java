package com.aistudio.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleCreateRequest {

    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 32, message = "角色编码长度需为 2-32 位")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "角色编码需使用大写字母、数字和下划线，且以字母开头")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称不能超过 50 个字符")
    private String roleName;
}
