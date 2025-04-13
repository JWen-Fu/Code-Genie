/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.zerodyn.plugin.config.ComponentConfig;
import com.zerodyn.plugin.config.DDDConfiguration;
import com.zerodyn.plugin.config.LayerConfig;
import com.zerodyn.plugin.template.TemplateInitializer;
import com.zerodyn.plugin.template.TemplateManager;
import freemarker.template.Template;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private final Project project;  // 改为保存Project对象
    private final FieldTypeMapper typeMapper;
    private final boolean useLombok;
    private final DDDConfiguration dddConfig;
    private final TemplateManager templateManager;

    public CodeGenerator(Project project,
                         FieldTypeMapper typeMapper,
                         boolean useLombok,
                         DDDConfiguration dddConfig) throws IOException {
        this.project = project;  // 存储Project对象
        this.typeMapper = typeMapper;
        this.useLombok = useLombok;
        this.dddConfig = dddConfig;
        this.templateManager = TemplateInitializer.createDefaultTemplateManager();
    }

    public void generateDDDCode(DDLParser.Table table) throws IOException {
        Map<String, Map<String, ComponentConfig>> allComponents = dddConfig.getAllValidComponents();
        List<String> layerOrder = Arrays.asList("domain", "application", "infrastructure", "interfaces");

        for (String layer : layerOrder) {
            Map<String, ComponentConfig> components = allComponents.get(layer);
            if (components != null) {
                components.forEach((compType, config) -> {
                    try {
                        generateComponent(table, layer, compType, config);
                    } catch (IOException e) {
                        log.error("生成失败: {}.{}", layer, compType, e);
                        Messages.showErrorDialog(project,
                                "生成" + layer + "层" + compType + "失败: " + e.getMessage(),
                                "错误");
                    }
                });
            }
        }
        Messages.showInfoMessage(project, "代码生成完成", "成功");
    }

    private Path getProjectBasePath() {
        // 从Project对象获取基础路径
        return Paths.get(project.getBasePath());
    }

    private void generateComponent(DDLParser.Table table,
                                   String layer,
                                   String componentType,
                                   ComponentConfig config) throws IOException {
        Map<String, Object> data = createTemplateData(table);
        // 添加转换后的组件类型
        data.put("componentType", toCamelCase(componentType));

        String content = processTemplate(config.getTemplateFile(), data);

        Path modulePath = getProjectBasePath()
                .resolve(dddConfig.getLayer(layer).getModuleName())
                .resolve("src/main/java");

        // 文件名使用转换后的类名
        String fileName = toCamelCase(table.getName()) + toCamelCase(componentType) + ".java";
        writeToFile(modulePath, config.getBasePackage(), fileName, content);
    }

    private Map<String, Object> createTemplateData(DDLParser.Table table) {
        Map<String, Object> data = new HashMap<>();
        data.put("className", toCamelCase(table.getName()));
        data.put("table", table);
        data.put("config", dddConfig);
        data.put("typeMapper", typeMapper);
        data.put("useLombok", useLombok);
        // 注意：componentType 现在在generateComponent方法中添加
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

    private void writeToFile(Path modulePath,
                             String packageName,
                             String fileName,
                             String content) throws IOException {
        Path packagePath = modulePath.resolve(packageName.replace(".", "/"));
        Files.createDirectories(packagePath);

        Path outputFile = packagePath.resolve(fileName);
        Files.write(outputFile, ("package " + packageName + ";\n\n" + content).getBytes());
    }

    // 增强的驼峰转换方法
    public static String toCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }

        // 处理特殊字符和多种分隔符
        String[] parts = name.split("[_\\-\\s]+");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }
}