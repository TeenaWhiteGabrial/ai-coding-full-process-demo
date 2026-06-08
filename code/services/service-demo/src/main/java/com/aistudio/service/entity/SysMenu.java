package com.aistudio.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_menu")
public class SysMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    private String name;
    private String path;
    private String component;
    private String icon;

    @Column(name = "menu_type")
    private String menuType;

    @Column(name = "permission_code")
    private String permissionCode;

    @Column(name = "app_code")
    private String appCode;

    private Integer sort;
    @Column(columnDefinition = "tinyint")
    private Integer hidden;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
