/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.zerodyn.plugin.config.DDDConfiguration;
import com.zerodyn.plugin.service.DDDConfigDialog;
import com.zerodyn.plugin.service.DDDConfigManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author JWen
 * @since 2025/3/25
 */
public class GenerateCodeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            showError("请先打开项目");
            return;
        }

        try {
            // 1. 获取DDL输入
            String ddl = getDDLFromUser();
            if (ddl == null) return;

            // 2. 解析DDL
            DDLParser.Table table = parseDDL(ddl);
            if (table == null) return;

            // 3. 处理类型映射
            FieldTypeMapper typeMapper = handleTypeMappings(table);
            if (typeMapper == null) return;

            // 4. 配置DDD架构
            DDDConfiguration dddConfig = configureDDD();
            if (dddConfig == null) return;

            // 5. 生成代码
            generateCode(project, table, typeMapper, dddConfig);

        } catch (Exception ex) {
            showError("生成过程中出错: " + ex.getMessage());
        }
    }

    private String getDDLFromUser() {
        GenerateCodeDialog ddlDialog = new GenerateCodeDialog();
        if (!ddlDialog.showAndGet()) {
            return null;
        }
        return ddlDialog.getDDL();
    }

    private DDLParser.Table parseDDL(String ddl) {
        DDLParser.Table table = new DDLParser().parseDDL(ddl);
        if (table == null || table.getColumns() == null || table.getColumns().isEmpty()) {
            showError("DDL解析失败或无字段定义");
            return null;
        }
        return table;
    }

    private FieldTypeMapper handleTypeMappings(DDLParser.Table table) {
        Set<String> requiredTypes = table.getColumns().stream()
                .map(col -> normalizeType(col.getType()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        FieldTypeMapper typeMapper = new FieldTypeMapper();
        Map<String, String> relevantMappings = typeMapper.getRelevantMappings(requiredTypes);

        TypeMappingDialog mappingDialog = new TypeMappingDialog(relevantMappings);
        if (!mappingDialog.showAndGet()) {
            return null;
        }

        try {
            typeMapper.savePartialMappings(mappingDialog.getModifiedMappings());
            return typeMapper;
        } catch (IOException e) {
            showError("类型映射保存失败: " + e.getMessage());
            return null;
        }
    }

    private DDDConfiguration configureDDD() throws IOException {
        DDDConfigManager configManager = new DDDConfigManager();
        DDDConfiguration dddConfig = configManager.loadConfiguration();

        DDDConfigDialog configDialog = new DDDConfigDialog(dddConfig);
        if (!configDialog.showAndGet()) {
            return null;
        }

        configManager.saveConfiguration(configDialog.getConfiguration());
        return configDialog.getConfiguration();
    }

    private void generateCode(Project project,
                              DDLParser.Table table,
                              FieldTypeMapper typeMapper,
                              DDDConfiguration dddConfig) throws IOException {
        CodeGenerator generator = new CodeGenerator(
                project.getBasePath() + "/src/main/java",
                typeMapper,
                true,
                dddConfig
        );

        generator.generateAll(table);
        showSuccess(table.getName());
    }

    private String normalizeType(String sqlType) {
        if (sqlType == null || sqlType.trim().isEmpty()) {
            return null;
        }
        String lowerType = sqlType.toLowerCase().trim();
        if (lowerType.startsWith("tinyint(1)")) {
            return "boolean";
        }
        return lowerType.replaceAll("\\(.*\\)", "").split("\\s+")[0];
    }

    private void showError(String message) {
        Messages.showErrorDialog(message, "错误");
    }

    private void showSuccess(String tableName) {
        String name = Arrays.stream(tableName.split("_"))
                .filter(part -> !part.isEmpty())
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(Collectors.joining());

        Messages.showInfoMessage(
                "成功生成:\n" +
                        "- " + name + "Entity.java\n" +
                        "- " + name + "Repository.java\n" +
                        "- " + name + "Service.java",
                "完成"
        );
    }
}