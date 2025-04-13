package com.zerodyn.plugin.config;

import java.util.Collections;
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
        return Collections.unmodifiableMap(new HashMap<>(components));
    }

    public boolean hasComponents() {
        return !components.isEmpty();
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
