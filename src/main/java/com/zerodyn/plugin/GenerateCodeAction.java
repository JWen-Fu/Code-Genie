package com.zerodyn.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author JWen
 * @since 2025/3/25
 */
public class GenerateCodeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("无法获取当前项目。", "错误");
            return;
        }
        String basePath = project.getBasePath();
        if (basePath == null) {
            Messages.showErrorDialog("无法获取项目根目录。", "错误");
            return;
        }

        // Create a FieldTypeMapper instance
        FieldTypeMapper fieldTypeMapper = new FieldTypeMapper();

        // Example: Add custom mapping (user can configure as needed)
        fieldTypeMapper.addMapping("decimal", "java.math.BigDecimal");

        GenerateCodeDialog dialog = new GenerateCodeDialog();
        dialog.show();
        if (dialog.isOK()) {
            String ddl = dialog.getDDL();
            String selectedArchitecture = dialog.getSelectedArchitecture();

            DDLParser ddlParser = new DDLParser();
            DDLParser.Table table = ddlParser.parseDDL(ddl);

            if (table == null) {
                Messages.showErrorDialog("DDL 解析失败，请检查输入格式。", "错误");
                return;
            }

            CodeGenerator codeGenerator = new CodeGenerator(basePath, fieldTypeMapper);
            try {
                codeGenerator.generateEntityClass(table);
                codeGenerator.generateControllerClass(table);
                codeGenerator.generateServiceClass(table);
                Messages.showInfoMessage("代码生成成功！\n生成文件：\n" +
                        table.getName() + "Entity.java\n" +
                        table.getName() + "Controller.java\n" +
                        table.getName() + "Service.java\n" +
                        "请在项目根目录下查看。", "成功");
            } catch (IOException ex) {
                Messages.showErrorDialog("生成代码时发生错误: " + ex.getMessage(), "错误");
            }
        }
    }
}
