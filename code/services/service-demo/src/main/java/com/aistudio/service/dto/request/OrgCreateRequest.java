package com.aistudio.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrgCreateRequest {

    @NotNull(message = "上级组织不能为空")
    private Long parentId;

    @NotBlank(message = "组织名称不能为空")
    @Size(max = 100, message = "组织名称不能超过 100 个字符")
    private String orgName;

    @NotBlank(message = "组织编码不能为空")
    @Size(max = 50, message = "组织编码不能超过 50 个字符")
    private String orgCode;

    @Size(max = 50, message = "负责人不能超过 50 个字符")
    private String leaderName;

    private Integer status;
}
