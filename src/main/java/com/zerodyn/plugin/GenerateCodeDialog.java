/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author JWen
 * @since 2025/3/25
 */
public class GenerateCodeDialog extends DialogWrapper {

    private JTextArea ddlTextArea;
    private JComboBox<String> architectureComboBox;

    public GenerateCodeDialog() {
        super(true);
        setTitle("请输入 DDL 语句并选择架构模式");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 上部分区：架构选择
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(new JLabel("选择架构模式："));
        architectureComboBox = new JComboBox<>(new String[]{"MVC", "DDD"});
        northPanel.add(architectureComboBox);
        panel.add(northPanel, BorderLayout.NORTH);

        // 中间区域：DDL 文本输入
        ddlTextArea = new JTextArea(10, 40);
        ddlTextArea.setText("""
                CREATE TABLE `User` (
                  `id` int(11) NOT NULL,
                  `name` varchar(255) DEFAULT NULL,
                  `email` varchar(255) DEFAULT NULL,
                  PRIMARY KEY (`id`)
                );
                """);
        JScrollPane scrollPane = new JScrollPane(ddlTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public String getDDL() {
        return ddlTextArea.getText();
    }

    public String getSelectedArchitecture() {
        return (String) architectureComboBox.getSelectedItem();
    }
}
