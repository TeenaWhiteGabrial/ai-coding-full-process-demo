package com.aistudio.service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrgTreeVO {

    private Long id;
    private Long parentId;
    private String orgName;
    private String orgCode;
    private String leaderName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrgTreeVO> children;
}
