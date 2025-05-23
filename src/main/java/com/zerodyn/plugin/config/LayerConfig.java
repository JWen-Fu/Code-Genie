/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JWen
 * @since 2025/4/5
 */
public class LayerConfig {
    private final Map<String, ComponentConfig> components = new HashMap<>();
    private String moduleName;

    public LayerConfig addComponent(String componentType, String basePackage, String template) {
        // 添加前验证组件类型
        if (componentType != null && !componentType.trim().isEmpty()) {
            components.put(componentType.trim(), new ComponentConfig(basePackage, template));
        }
        return this;
    }

    public Map<String, ComponentConfig> getComponents() {
        // 返回不可修改的副本
        return Map.copyOf(components);
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
