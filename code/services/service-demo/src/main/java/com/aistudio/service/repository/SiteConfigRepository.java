package com.aistudio.service.repository;

import com.aistudio.service.entity.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteConfigRepository extends JpaRepository<SiteConfig, Long> {
}
