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
import org.slf4j.LoggerFactory;

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
            String ddl = getDDLFromUser(project);
            if (ddl == null) return;

            // 2. 解析DDL
            DDLParser.Table table = parseDDL(project, ddl);
            if (table == null) return;

            // 3. 处理类型映射
            FieldTypeMapper typeMapper = handleTypeMappings(project, table);
            if (typeMapper == null) return;

            // 4. 配置DDD架构
            DDDConfiguration dddConfig = configureDDD(project);
            if (dddConfig == null) return;

            // 5. 生成代码
            generateCode(project, table, typeMapper, dddConfig);

        } catch (Exception ex) {
            showError(project, "生成过程中出错: " +
                    (ex.getMessage() != null ? ex.getMessage() : "未知错误"));
        }
    }

    private String getDDLFromUser(Project project) {
        GenerateCodeDialog ddlDialog = new GenerateCodeDialog(project);
        return ddlDialog.showAndGet() ? ddlDialog.getDDL() : null;
    }

    private FieldTypeMapper handleTypeMappings(Project project, DDLParser.Table table) {
        try {
            Set<String> requiredTypes = table.getColumns().stream()
                    .map(col -> normalizeType(col.getType()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            FieldTypeMapper typeMapper = new FieldTypeMapper();
            Map<String, String> relevantMappings = typeMapper.getRelevantMappings(requiredTypes);

            TypeMappingDialog mappingDialog = new TypeMappingDialog(relevantMappings);

            if (mappingDialog.showAndGet()) {
                typeMapper.savePartialMappings(mappingDialog.getModifiedMappings());
                return typeMapper;
            }
        } catch (Exception e) {
            showError(project, "类型映射配置失败: " + e.getMessage());
        }
        return null;
    }

    private DDDConfiguration configureDDD(Project project) {
        try {
            DDDConfigManager configManager = new DDDConfigManager();
            DDDConfiguration config = configManager.loadConfiguration();

            // 确保配置对象有效
            if (config == null) {
                throw new IllegalStateException("配置初始化失败");
            }

            DDDConfigDialog configDialog = new DDDConfigDialog(project, config);
            if (configDialog.showAndGet()) {
                DDDConfiguration updatedConfig = configDialog.getConfiguration();
                configManager.saveConfiguration(updatedConfig);
                return updatedConfig;
            }
        } catch (Exception e) {
            String errorMsg = "配置加载失败: " + (e.getMessage() != null ? e.getMessage() : "未知原因");
            Messages.showErrorDialog(project, errorMsg, "配置错误");
            // 记录完整错误日志
            LoggerFactory.getLogger(getClass()).error("配置加载错误", e);
        }
        return null;
    }

    private void generateCode(Project project,
                              DDLParser.Table table,
                              FieldTypeMapper typeMapper,
                              DDDConfiguration dddConfig) throws IOException {
        new CodeGenerator(project, typeMapper, true, dddConfig)
                .generateDDDCode(table);
    }

    private void showError(Project project, String message) {
        Messages.showErrorDialog(project, message, "错误");
    }

    private DDLParser.Table parseDDL(Project project, String ddl) {
        DDLParser.Table table = new DDLParser().parseDDL(ddl);
        if (table == null || table.getColumns() == null || table.getColumns().isEmpty()) {
            showError(project, "DDL解析失败或无字段定义");  // 添加project参数
            return null;
        }
        return table;
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
}