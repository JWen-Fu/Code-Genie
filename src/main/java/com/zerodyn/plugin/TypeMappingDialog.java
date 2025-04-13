/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author JWen
 * @since 2025/3/26
 */
public class TypeMappingDialog extends DialogWrapper {
    private JPanel mainPanel;
    private JPanel mappingPanel;
    private final Map<String, String> initialMappings;

    public TypeMappingDialog(Map<String, String> relevantMappings) {
        super(false);
        this.initialMappings = Objects.requireNonNull(relevantMappings);
        setTitle("数据库类型映射配置");
        initUI();
        init();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 帮助提示
        JLabel helpLabel = new JLabel(
                "<html><b>提示：</b> 配置数据库类型到Java类型的映射关系<br>" +
                        "示例：varchar → String, tinyint(1) → Boolean</html>"
        );
        mainPanel.add(helpLabel, BorderLayout.NORTH);

        // 映射面板
        mappingPanel = new JPanel();
        mappingPanel.setLayout(new BoxLayout(mappingPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(mappingPanel);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮
        JButton addButton = new JButton("+ 添加映射");
        addButton.addActionListener(e -> addMappingRow("", ""));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 加载初始映射
        loadMappings(initialMappings);
    }

    private void loadMappings(Map<String, String> mappings) {
        SwingUtilities.invokeLater(() -> {
            mappingPanel.removeAll();
            mappings.forEach(this::addMappingRow);
            mappingPanel.revalidate();
            mappingPanel.repaint();
        });
    }

    private void addMappingRow(String dbType, String javaType) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JTextField dbField = new JTextField(dbType, 15);
        JTextField javaField = new JTextField(javaType, 20);
        JButton removeBtn = new JButton("×");

        rowPanel.add(new JLabel("数据库类型:"));
        rowPanel.add(dbField);
        rowPanel.add(new JLabel("→"));
        rowPanel.add(new JLabel("Java类型:"));
        rowPanel.add(javaField);
        rowPanel.add(removeBtn);

        removeBtn.addActionListener(e -> {
            mappingPanel.remove(rowPanel);
            mappingPanel.revalidate();
            mappingPanel.repaint();
        });

        mappingPanel.add(rowPanel);
    }

    public Map<String, String> getModifiedMappings() {
        Map<String, String> result = new HashMap<>();
        for (Component comp : mappingPanel.getComponents()) {
            if (comp instanceof JPanel row) {
                JTextField dbField = (JTextField) row.getComponent(1);
                JTextField javaField = (JTextField) row.getComponent(4);
                if (!dbField.getText().trim().isEmpty()) {
                    result.put(dbField.getText().trim().toLowerCase(),
                            javaField.getText().trim());
                }
            }
        }
        return result;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mainPanel;
    }
}