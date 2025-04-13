/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin.config;

import java.util.Objects;

/**
 * @author JWen
 * @since 2025/4/5
 */
public class ComponentConfig {
    private String basePackage;
    private String templateFile;

    public ComponentConfig(String basePackage, String templateFile) {
        this.basePackage = Objects.requireNonNull(basePackage, "Base package cannot be null");
        this.templateFile = Objects.requireNonNull(templateFile, "Template file cannot be null");
    }

    public void validate() {
        if (basePackage.isBlank()) {
            throw new IllegalStateException("Base package cannot be blank");
        }
        if (templateFile.isBlank()) {
            throw new IllegalStateException("Template file cannot be blank");
        }
    }

    public String getBasePackage() {
        // 确保包路径以com.开头
        return basePackage.startsWith("com.") || basePackage.startsWith("org.") ?
                basePackage : "com." + basePackage;
    }

    public String getTemplateFile() { return templateFile; }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }
}
