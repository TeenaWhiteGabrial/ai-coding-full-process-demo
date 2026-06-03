package com.aistudio.service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DictTreeVO {
    private Long id;
    private Long parentId;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private Integer status;
    private Integer sort;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DictTreeVO> children = new ArrayList<>();
}
