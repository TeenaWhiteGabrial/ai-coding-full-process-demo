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
@Table(name = "site_config")
public class SiteConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_name")
    private String siteName;

    @Column(name = "site_description")
    private String siteDescription;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "footer_text")
    private String footerText;

    @Column(name = "footer_copyright")
    private String footerCopyright;

    @Column(name = "footer_record")
    private String footerRecord;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
