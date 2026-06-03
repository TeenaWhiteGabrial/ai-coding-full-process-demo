package com.aistudio.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictCreateRequest {

    private Long parentId;

    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @NotBlank(message = "字典名称不能为空")
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    private Integer status;
    private Integer sort;
    private String remark;
}
