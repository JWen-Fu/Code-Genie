/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.zerodyn.plugin.config.DDDConfiguration;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author JWen
 * @since 2025/4/5
 */
public class DDDConfigManager {
    private static final String CONFIG_DIR = ".codegen";
    private static final String CONFIG_FILE = "ddd-config.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public DDDConfiguration loadConfiguration() throws IOException {
        Path configPath = getConfigPath();

        if (!Files.exists(configPath)) {
            return createAndSaveDefaultConfig();
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            DDDConfiguration config = GSON.fromJson(reader, DDDConfiguration.class);
            return validateConfig(config);
        } catch (JsonSyntaxException e) {
            // 如果配置文件损坏，重新创建默认配置
            return createAndSaveDefaultConfig();
        }
    }

    private DDDConfiguration createAndSaveDefaultConfig() throws IOException {
        DDDConfiguration defaultConfig = new DDDConfiguration();
        saveConfiguration(defaultConfig);
        return defaultConfig;
    }

    private DDDConfiguration validateConfig(DDDConfiguration config) {
        if (config == null) {
            return new DDDConfiguration();
        }
        return config;
    }

    public void saveConfiguration(DDDConfiguration config) throws IOException {
        Objects.requireNonNull(config, "配置不能为null");
        Path configPath = getConfigPath();
        Files.createDirectories(configPath.getParent());

        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(config, writer);
        }
    }

    private Path getConfigPath() {
        return Paths.get(System.getProperty("user.home"), CONFIG_DIR, CONFIG_FILE);
    }
}
