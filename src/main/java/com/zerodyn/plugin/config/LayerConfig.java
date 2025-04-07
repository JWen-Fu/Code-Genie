package com.zerodyn.plugin.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author JWen
 * @since 2025/4/5
 */
public class LayerConfig {
    private final Map<String, ComponentConfig> components = new HashMap<>();

    public LayerConfig addComponent(String componentType, String basePackage, String template) {
        Objects.requireNonNull(componentType, "Component type cannot be null");
        Objects.requireNonNull(basePackage, "Base package cannot be null");
        Objects.requireNonNull(template, "Template file cannot be null");

        components.put(componentType, new ComponentConfig(basePackage, template));
        return this;
    }

    public Map<String, ComponentConfig> getComponents() {
        return Collections.unmodifiableMap(new HashMap<>(components));
    }

    public boolean hasComponents() {
        return !components.isEmpty();
    }
}
