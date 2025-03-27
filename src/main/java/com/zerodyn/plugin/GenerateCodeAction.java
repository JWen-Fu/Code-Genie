package com.zerodyn.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
            Messages.showErrorDialog("No project found", "Error");
            return;
        }

        // 1. 首先弹出DDL输入对话框
        GenerateCodeDialog dialog = new GenerateCodeDialog();
        if (!dialog.showAndGet()) {
            return; // 用户取消输入
        }

        // 2. 解析DDL获取表结构和需要的类型
        DDLParser parser = new DDLParser();
        DDLParser.Table table = parser.parseDDL(dialog.getDDL());
        if (table == null) {
            Messages.showErrorDialog("Invalid DDL format", "Error");
            return;
        }

        // 3. 提取DDL中出现的字段类型
        Set<String> requiredTypes = table.getColumns().stream()
                .map(col -> col.getType().split("\\(")[0].toLowerCase())
                .collect(Collectors.toSet());

        // 4. 初始化类型映射器并加载配置
        FieldTypeMapper typeMapper = new FieldTypeMapper();

        // 5. 弹出类型映射配置对话框(仅显示DDL中需要的类型)
        TypeMappingDialog mappingDialog = new TypeMappingDialog(
                typeMapper.getRelevantMappings(requiredTypes)
        );
        if (mappingDialog.showAndGet()) {
            try {
                // 6. 保存用户修改的映射配置
                typeMapper.savePartialMappings(mappingDialog.getModifiedMappings());
            } catch (IOException ex) {
                Messages.showErrorDialog("配置保存失败: " + ex.getMessage(), "错误");
            }
        }

        // 7. 生成代码
        try {
            new CodeGenerator(project.getBasePath() + "/src/main/java", typeMapper, true)
                    .generateAll(table);
            Messages.showInfoMessage(
                    "Generated:\n" +
                            "- " + toCamelCase(table.getName()) + "Entity.java\n" +
                            "- " + toCamelCase(table.getName()) + "Service.java\n" +
                            "- " + toCamelCase(table.getName()) + "Controller.java",
                    "Success"
            );
        } catch (IOException ex) {
            Messages.showErrorDialog("Generation failed: " + ex.getMessage(), "Error");
        }
    }

    private String toCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        for (String part : name.split("_")) {
            if (!part.isEmpty()) {
                result.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }
}
