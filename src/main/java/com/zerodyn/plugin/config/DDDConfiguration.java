package com.zerodyn.plugin.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author JWen
 * @since 2025/4/5
 */
public class DDDConfiguration {
    private Map<String, LayerConfig> layers = new LinkedHashMap<>();
    private boolean enableCQRS = false;

    public DDDConfiguration() {
        this.layers = new LinkedHashMap<>();
        initializeDefaultLayers();
    }

    private void initializeDefaultLayers() {
        this.layers.put("domain", createDomainLayer());
        this.layers.put("application", createApplicationLayer());
        this.layers.put("infrastructure", createInfrastructureLayer());
        this.layers.put("interfaces", createInterfacesLayer());
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

    public Map<String, ComponentConfig> getComponents(String layerName) {
        LayerConfig layer = getLayer(layerName);
        return layer != null ? layer.getComponents() : Map.of();
    }

    public Map<String, LayerConfig> getLayers() {
        return layers;
    }

    public Map<String, Map<String, ComponentConfig>> getAllValidComponents() {
        Map<String, Map<String, ComponentConfig>> result = new LinkedHashMap<>();
        layers.forEach((layerName, layerConfig) -> {
            if (!layerConfig.getComponents().isEmpty()) {
                result.put(layerName, layerConfig.getComponents());
            }
        });
        return result;
    }

    public boolean isEnableCQRS() {
        return enableCQRS;
    }

    public void setEnableCQRS(boolean enableCQRS) {
        this.enableCQRS = enableCQRS;
    }
}
