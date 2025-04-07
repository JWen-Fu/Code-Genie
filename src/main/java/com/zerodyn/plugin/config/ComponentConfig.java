package com.zerodyn.plugin.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author JWen
 * @since 2025/4/5
 */
public class ComponentConfig {
    private String basePackage;
    private String templateFile;
    private final Map<String, String> additionalConfig;

    public ComponentConfig(String basePackage, String templateFile) {
        this(basePackage, templateFile, new HashMap<>());
    }

    public ComponentConfig(String basePackage, String templateFile, Map<String, String> additionalConfig) {
        this.basePackage = Objects.requireNonNull(basePackage);
        this.templateFile = Objects.requireNonNull(templateFile);
        this.additionalConfig = new HashMap<>(Objects.requireNonNull(additionalConfig));
    }

    // Getters and setters
    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = Objects.requireNonNull(basePackage);
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = Objects.requireNonNull(templateFile);
    }

    public Map<String, String> getAdditionalConfig() {
        return new HashMap<>(additionalConfig);
    }

    public void addConfigItem(String key, String value) {
        additionalConfig.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
    }
}
