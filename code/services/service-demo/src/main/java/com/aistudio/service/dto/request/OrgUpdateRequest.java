package com.aistudio.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrgUpdateRequest extends OrgCreateRequest {

    @NotNull(message = "组织 ID 不能为空")
    private Long id;
}
