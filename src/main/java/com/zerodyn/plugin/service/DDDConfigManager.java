package com.zerodyn.plugin.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zerodyn.plugin.config.DDDConfiguration;
import com.zerodyn.plugin.config.LayerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author JWen
 * @since 2025/4/5
 */
public class DDDConfigManager {
    private static final String CONFIG_DIR = ".codegen";
    private static final String CONFIG_FILE = "ddd-config.json";
    private static final String DEFAULT_CONFIG = "/default-ddd-config.json";
    private static final List<String> REQUIRED_LAYERS = Arrays.asList(
            "domain", "application", "infrastructure", "interfaces"
    );

    public DDDConfiguration loadConfiguration() throws IOException {
        Path configPath = getConfigPath();

        // 首次运行时创建完整默认配置
        if (!Files.exists(configPath)) {
            createCompleteDefaultConfig(configPath);
        }

        // 读取并验证配置
        DDDConfiguration config;
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(configPath))) {
            config = new Gson().fromJson(reader, DDDConfiguration.class);
        }

        // 确保所有必要层都存在
        validateAndCompleteConfig(config);
        return config;
    }

    private void createCompleteDefaultConfig(Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent());

        // 使用DDDConfiguration的默认构造生成完整配置
        DDDConfiguration defaultConfig = new DDDConfiguration();
        saveConfiguration(defaultConfig);
    }

    private void validateAndCompleteConfig(DDDConfiguration config) {
        // 确保所有必要层都存在
        for (String layer : REQUIRED_LAYERS) {
            if (!config.getLayers().containsKey(layer)) {
                config.getLayers().put(layer, new LayerConfig());
            }
        }
    }

    public void saveConfiguration(DDDConfiguration config) throws IOException {
        // 保存前验证配置完整性
        validateAndCompleteConfig(config);

        Path configPath = getConfigPath();
        Files.createDirectories(configPath.getParent());

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        Files.writeString(configPath, gson.toJson(config));
    }

    private Path getConfigPath() {
        return Paths.get(System.getProperty("user.home"), CONFIG_DIR, CONFIG_FILE);
    }

    private void createDefaultConfig(Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent());

        // 检查资源文件是否存在
        try (InputStream is = getClass().getResourceAsStream(DEFAULT_CONFIG)) {
            if (is != null) {
                Files.copy(is, targetPath);
            } else {
                // 硬编码默认配置
                String defaultConfig = "{ \"layers\": { \"infrastructure\": { \"components\": { \"RepositoryImpl\": { \"basePackage\": \"infrastructure.persistence\", \"templateFile\": \"RepositoryTemplate.ftl\" } } } } }";
                Files.writeString(targetPath, defaultConfig);

                // 确保模板文件存在
                copyDefaultTemplate("RepositoryTemplate.ftl");
            }
        }
    }

    private void copyDefaultTemplate(String templateName) throws IOException {
        Path templateDir = Paths.get(System.getProperty("user.home"), ".codegen", "templates");
        Files.createDirectories(templateDir);

        try (InputStream is = getClass().getResourceAsStream("/templates/ddd/" + templateName)) {
            if (is != null) {
                Files.copy(is, templateDir.resolve(templateName));
            } else {
                // 创建基本模板
                String content = templateName.equals("RepositoryTemplate.ftl") ?
                        "<#-- 默认Repository实现模板 -->\npackage ${config.layers['infrastructure'].components['RepositoryImpl'].basePackage};\n\npublic class Jpa${table.name}RepositoryImpl {}" :
                        "";
                Files.writeString(templateDir.resolve(templateName), content);
            }
        }
    }
}
