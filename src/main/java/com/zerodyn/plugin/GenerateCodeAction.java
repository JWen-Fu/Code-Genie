package com.zerodyn.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author JWen
 * @since 2025/3/25
 */
public class GenerateCodeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 使用文本域让用户输入 DDL，采用文本块（JDK 21 支持）提高可读性
        JTextArea ddlTextArea = new JTextArea(10, 40);
        ddlTextArea.setText("""
                CREATE TABLE `User` (
                  `id` int(11) NOT NULL,
                  `name` varchar(255) DEFAULT NULL,
                  `email` varchar(255) DEFAULT NULL,
                  PRIMARY KEY (`id`)
                );
                """);

        // 提供架构选择：MVC 或 DDD
        String[] architectures = {"MVC", "DDD"};
        JComboBox<String> architectureComboBox = new JComboBox<>(architectures);

        // 构建面板：上部放置架构选择，下部为 DDL 文本输入
        JPanel panel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(new JLabel("选择架构模式："));
        northPanel.add(architectureComboBox);
        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(ddlTextArea), BorderLayout.CENTER);

        // 显示对话框
        int result = Messages.showOkCancelDialog(panel, "请输入 DDL 语句并选择架构模式", "代码生成", "生成", "取消", null);
        if (result == Messages.OK) {
            var ddl = ddlTextArea.getText();
            var selectedArchitecture = (String) architectureComboBox.getSelectedItem();

            // 此处我们对 MVC 与 DDD 目前均生成相同的三类文件，后续可根据 selectedArchitecture 进行不同处理
            DDLParser ddlParser = new DDLParser();
            DDLParser.Table table = ddlParser.parseDDL(ddl);

            if (table == null) {
                Messages.showErrorDialog("DDL 解析失败，请检查输入格式。", "错误");
                return;
            }

            CodeGenerator codeGenerator = new CodeGenerator();
            try {
                // 生成 Entity、Controller、Service 类
                codeGenerator.generateEntityClass(table);
                codeGenerator.generateControllerClass(table);
                codeGenerator.generateServiceClass(table);

                // 成功后弹出提示
                Messages.showInfoMessage("代码生成成功！\n生成文件：\n" +
                        table.getName() + "Entity.java\n" +
                        table.getName() + "Controller.java\n" +
                        table.getName() + "Service.java", "成功");
            } catch (IOException ex) {
                Messages.showErrorDialog("生成代码时发生错误: " + ex.getMessage(), "错误");
            }
        }
    }
}
