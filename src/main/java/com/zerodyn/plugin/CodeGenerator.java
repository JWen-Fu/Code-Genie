/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin;

import com.zerodyn.plugin.config.ComponentConfig;
import com.zerodyn.plugin.config.DDDConfiguration;
import com.zerodyn.plugin.config.LayerConfig;
import com.zerodyn.plugin.template.TemplateInitializer;
import com.zerodyn.plugin.template.TemplateManager;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JWen
 * @since 2025/3/24
 */
@Slf4j
public class CodeGenerator {
    private static final Logger log = LoggerFactory.getLogger(CodeGenerator.class);
    private final String basePath;
    private final FieldTypeMapper typeMapper;
    private final boolean useLombok;
    private final DDDConfiguration dddConfig;
    private final TemplateManager templateManager;

    public CodeGenerator(String basePath,
                         FieldTypeMapper typeMapper,
                         boolean useLombok,
                         DDDConfiguration dddConfig) throws IOException {
        this.basePath = basePath;
        this.typeMapper = typeMapper;
        this.useLombok = useLombok;
        this.dddConfig = dddConfig;

        try {
            // 初始化模板系统
            TemplateInitializer.ensureDefaultTemplates();
            this.templateManager = TemplateInitializer.createDefaultTemplateManager();
        } catch (TemplateException e) {
            throw new IOException("Failed to initialize template system", e);
        }

        ensureBaseDirectoryExists();
    }

    private void ensureBaseDirectoryExists() throws IOException {
        Path path = Paths.get(basePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public void generateAll(DDLParser.Table table) throws IOException {
        generateDDDCode(table);
    }

    private void generateDDDCode(DDLParser.Table table) throws IOException {
        // 使用安全的组件获取方式
        Map<String, Map<String, ComponentConfig>> allComponents =
                dddConfig.getAllValidComponents();

        // 按固定顺序处理各层
        List<String> layerOrder = Arrays.asList(
                "domain", "application", "infrastructure", "interfaces"
        );

        for (String layer : layerOrder) {
            Map<String, ComponentConfig> components = allComponents.get(layer);
            if (components != null) {
                components.forEach((compType, config) -> {
                    try {
                        generateComponent(table, compType, config);
                    } catch (IOException e) {
                        log.error("Failed to generate {} for layer {}", compType, layer, e);
                    }
                });
            }
        }
    }

    private void generateComponent(DDLParser.Table table,
                                   String componentType,
                                   ComponentConfig config) throws IOException {
        try {
            // 准备模板数据
            Map<String, Object> data = createTemplateData(table);

            // 处理模板
            String content = processTemplate(config.getTemplateFile(), data);

            // 写入文件
            writeToFile(
                    config.getBasePackage(),
                    toCamelCase(table.getName()) + componentType + ".java",
                    content
            );

        } catch (Exception e) {
            throw new IOException("Failed to generate " + componentType + ": " + e.getMessage(), e);
        }
    }

    private Map<String, Object> createTemplateData(DDLParser.Table table) {
        Map<String, Object> data = new HashMap<>();
        data.put("table", table);
        data.put("config", dddConfig);
        data.put("typeMapper", typeMapper);
        data.put("useLombok", useLombok);
        return data;
    }

    private String processTemplate(String templateName, Map<String, Object> data)
            throws IOException {
        try {
            Template template = templateManager.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new IOException("Template processing failed: " + templateName, e);
        }
    }

    private void writeToFile(String packageName, String fileName, String content)
            throws IOException {
        String packagePath = packageName.replace(".", "/");
        Path outputDir = Paths.get(basePath, packagePath);

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        Path outputFile = outputDir.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write("package " + packageName + ";\n\n");
            writer.write(content);
        }
    }

    private String toCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        for (String part : name.split("_")) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }
}