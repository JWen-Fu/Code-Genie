/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author JWen
 * @since 2025/4/5
 */
public class DDDConfiguration {
    private final Map<String, LayerConfig> layers;
    private boolean enableCQRS = false;

    public DDDConfiguration() {
        this.layers = new LinkedHashMap<>();
        initializeDefaultLayers();
    }

    private void initializeDefaultLayers() {
        // 只初始化我们需要的组件
        layers.put("domain", createDomainLayer());
        layers.put("application", createApplicationLayer());
        layers.put("infrastructure", createInfrastructureLayer());
        layers.put("interfaces", createInterfacesLayer());
    }

    private boolean isValidComponentType(String layer, String componentType) {
        // 定义每个层允许的组件类型白名单
        Map<String, Set<String>> validComponents = Map.of(
                "domain", Set.of("Entity", "Repository"),
                "application", Set.of("Service", "DTO"),
                "infrastructure", Set.of("RepositoryImpl"),
                "interfaces", Set.of("Controller")
        );
        return validComponents.getOrDefault(layer, Collections.emptySet())
                .contains(componentType);
    }

    public Map<String, Map<String, ComponentConfig>> getAllValidComponents() {
        Map<String, Map<String, ComponentConfig>> result = new LinkedHashMap<>();
        layers.forEach((layerName, layerConfig) -> {
            Map<String, ComponentConfig> validComponents = new HashMap<>();
            layerConfig.getComponents().forEach((compType, config) -> {
                // 严格过滤组件类型
                if (isValidComponentType(layerName, compType)) {
                    validComponents.put(compType, config);
                }
            });
            if (!validComponents.isEmpty()) {
                result.put(layerName, validComponents);
            }
        });
        return result;
    }

    private LayerConfig createDomainLayer() {
        return new LayerConfig()
                .addComponent("Entity", "domain.model", "EntityTemplate.ftl")
                .addComponent("Repository", "domain.repository", "RepositoryTemplate.ftl");
    }

    private LayerConfig createApplicationLayer() {
        return new LayerConfig()
                .addComponent("Service", "application.service", "ServiceTemplate.ftl")
                .addComponent("DTO", "application.dto", "DtoTemplate.ftl");
    }

    private LayerConfig createInfrastructureLayer() {
        return new LayerConfig()
                .addComponent("RepositoryImpl", "infrastructure.persistence", "RepositoryImplTemplate.ftl");
    }

    private LayerConfig createInterfacesLayer() {
        return new LayerConfig()
                .addComponent("Controller", "interfaces.rest", "ControllerTemplate.ftl");
    }

    public LayerConfig getLayer(String layerName) {
        Objects.requireNonNull(layerName, "Layer name cannot be null");
        return layers.computeIfAbsent(layerName, k -> new LayerConfig());
    }

    public Map<String, LayerConfig> getLayers() {
        return layers;
    }

    public boolean isEnableCQRS() {
        return enableCQRS;
    }

    public void setEnableCQRS(boolean enableCQRS) {
        this.enableCQRS = enableCQRS;
    }
}
