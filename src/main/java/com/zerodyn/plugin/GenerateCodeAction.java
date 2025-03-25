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
        // 获取当前项目
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

        // 显示自定义对话框获取用户输入
        GenerateCodeDialog dialog = new GenerateCodeDialog();
        dialog.show();
        if (dialog.isOK()) {
            String ddl = dialog.getDDL();
            String selectedArchitecture = dialog.getSelectedArchitecture();

            // 根据所选架构选择不同处理逻辑，目前MVC与DDD均生成相同代码，但可以在此区分
            if ("DDD".equalsIgnoreCase(selectedArchitecture)) {
                // 暂时提示DDD模式与MVC模式生成代码相同，后续可扩展
                Messages.showInfoMessage("当前为 DDD 模式（注意：生成代码与 MVC 模式相同，后续版本将扩展DDD专属代码）", "提示");
            }

            DDLParser ddlParser = new DDLParser();
            DDLParser.Table table = ddlParser.parseDDL(ddl);

            if (table == null) {
                Messages.showErrorDialog("DDL 解析失败，请检查输入格式。", "错误");
                return;
            }

            CodeGenerator codeGenerator = new CodeGenerator(basePath);
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
