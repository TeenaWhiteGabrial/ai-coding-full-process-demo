package com.aistudio.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dict")
public class SysDict {

    @TableId(type = IdType.AUTO)
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
}
