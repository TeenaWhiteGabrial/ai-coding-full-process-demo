package com.aistudio.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_org")
public class SysOrg {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String orgName;
    private String orgCode;
    private String leaderName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
