package com.aistudio.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuRequest {

    @NotBlank(message = "menu name can not be blank")
    private String name;

    private Long parentId;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Integer hidden;
}
