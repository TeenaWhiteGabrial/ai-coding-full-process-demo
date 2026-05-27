package com.aistudio.service.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class OrgOptionVO {

    private Long id;
    private Long parentId;
    private String orgName;
    private String orgCode;
    private Integer status;
    private List<OrgOptionVO> children;
}
