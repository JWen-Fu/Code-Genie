package com.zerodyn.plugin.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
        this.layers.put("domain", new LayerConfig()
                .addComponent("Entity", "domain.model", "EntityTemplate.ftl")
                .addComponent("Repository", "domain.repository", "RepositoryTemplate.ftl"));

        this.layers.put("application", new LayerConfig()
                .addComponent("Service", "application.service", "ServiceTemplate.ftl")
                .addComponent("DTO", "application.dto", "DtoTemplate.ftl"));

        this.layers.put("infrastructure", new LayerConfig()
                .addComponent("RepositoryImpl", "infrastructure.persistence", "RepositoryImplTemplate.ftl"));

        this.layers.put("interfaces", new LayerConfig()
                .addComponent("Controller", "interfaces.rest", "ControllerTemplate.ftl"));
    }

    public Map<String, Map<String, ComponentConfig>> getAllComponents() {
        Map<String, Map<String, ComponentConfig>> result = new LinkedHashMap<>();
        layers.forEach((layerName, layerConfig) -> {
            result.put(layerName, new LinkedHashMap<>(layerConfig.getComponents()));
        });
        return result;
    }

    public LayerConfig getLayer(String layerName) {
        Objects.requireNonNull(layerName, "Layer name cannot be null");
        return layers.computeIfAbsent(layerName, k -> new LayerConfig());
    }

    public Map<String, LayerConfig> getLayers() {
        return new LinkedHashMap<>(layers);
    }

    public boolean isEnableCQRS() {
        return enableCQRS;
    }

    public void setEnableCQRS(boolean enableCQRS) {
        this.enableCQRS = enableCQRS;
    }
}