package com.zerodyn.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
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

        // 1. 获取DDL输入
        GenerateCodeDialog ddlDialog = new GenerateCodeDialog();
        if (!ddlDialog.showAndGet()) {
            return;
        }

        // 2. 解析DDL
        DDLParser.Table table = new DDLParser().parseDDL(ddlDialog.getDDL());
        if (table == null || table.getColumns() == null || table.getColumns().isEmpty()) {
            showError("DDL解析失败或无字段定义");
            return;
        }

        // 3. 提取需要配置的类型
        Set<String> requiredTypes = table.getColumns().stream()
                .map(col -> normalizeType(col.getType()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4. 获取类型映射
        FieldTypeMapper typeMapper = new FieldTypeMapper();
        Map<String, String> relevantMappings = typeMapper.getRelevantMappings(requiredTypes);

        // 5. 显示配置对话框
        TypeMappingDialog mappingDialog = new TypeMappingDialog(relevantMappings);
        if (!mappingDialog.showAndGet()) {
            return;
        }

        // 6. 保存配置并生成代码
        try {
            typeMapper.savePartialMappings(mappingDialog.getModifiedMappings());
            generateCode(project, table, typeMapper);
        } catch (IOException ex) {
            showError("配置保存失败: " + ex.getMessage());
        }
    }

    private String normalizeType(String sqlType) {
        if (sqlType == null || sqlType.trim().isEmpty()) {
            return null;
        }
        String lowerType = sqlType.toLowerCase().trim();

        // 特殊处理tinyint(1)
        if (lowerType.startsWith("tinyint(1)")) {
            return "boolean";
        }

        // 去除括号内容
        return lowerType.replaceAll("\\(.*\\)", "").split("\\s+")[0];
    }

    private void generateCode(Project project, DDLParser.Table table, FieldTypeMapper typeMapper)
            throws IOException {
        new CodeGenerator(
                project.getBasePath() + "/src/main/java",
                typeMapper,
                true
        ).generateAll(table);

        showSuccess(table.getName());
    }

    private void showError(String message) {
        Messages.showErrorDialog(message, "错误");
    }

    private void showSuccess(String tableName) {
        String name = toCamelCase(tableName);
        Messages.showInfoMessage(
                "成功生成:\n- " + name + "Entity.java\n" +
                        "- " + name + "Service.java\n" +
                        "- " + name + "Controller.java",
                "完成"
        );
    }

    private String toCamelCase(String name) {
        return Arrays.stream(name.split("_"))
                .filter(part -> !part.isEmpty())
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(Collectors.joining());
    }
}