package com.zerodyn.plugin;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JWen
 * @since 2025/3/26
 */
public class TypeMappingDialog extends DialogWrapper {
    private final List<MappingRow> rows = new ArrayList<>();
    private static final int ROW_HEIGHT = 30;
    private static final int MIN_ROWS = 3;
    private static final int MAX_ROWS = 10;

    private final JPanel mainPanel;
    private final JPanel mappingPanel;
    private int currentRowCount = 0;

    public TypeMappingDialog(Map<String, String> relevantMappings) {
        super(true);
        setTitle("字段类型映射配置");

        // 主面板设置
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 映射面板（使用垂直BoxLayout）
        mappingPanel = new JPanel();
        mappingPanel.setLayout(new BoxLayout(mappingPanel, BoxLayout.Y_AXIS));

        // 动态计算初始高度
        currentRowCount = relevantMappings.size();
        int initialHeight = Math.min(
                Math.max(currentRowCount, MIN_ROWS),
                MAX_ROWS
        ) * ROW_HEIGHT + 60; // 60是按钮和边距

        // 滚动面板设置
        JScrollPane scrollPane = new JScrollPane(mappingPanel);
        scrollPane.setPreferredSize(new Dimension(500, initialHeight));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 添加组件
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JButton addButton = new JButton("+ 添加映射");
        addButton.addActionListener(e -> addMappingRow("", ""));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        buttonPanel.add(addButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 添加已有映射
        relevantMappings.forEach(this::addMappingRow);

        init();
    }

    private void addMappingRow(String dbType, String javaType) {
        if (currentRowCount >= MAX_ROWS) {
            return; // 达到最大行数限制
        }

        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        JTextField dbTypeField = new JTextField(dbType, 12);
        JTextField javaTypeField = new JTextField(javaType, 20);
        JButton removeBtn = new JButton("×");

        // 组件组装
        rowPanel.add(new JLabel("DB类型:"));
        rowPanel.add(dbTypeField);
        rowPanel.add(Box.createHorizontalStrut(5));
        rowPanel.add(new JLabel("→"));
        rowPanel.add(Box.createHorizontalStrut(5));
        rowPanel.add(new JLabel("Java类型:"));
        rowPanel.add(javaTypeField);
        rowPanel.add(Box.createHorizontalStrut(10));
        rowPanel.add(removeBtn);

        // 删除功能
        removeBtn.addActionListener(e -> {
            mappingPanel.remove(rowPanel);
            currentRowCount--;
            updateDialogSize();
            mappingPanel.revalidate();
        });

        mappingPanel.add(rowPanel);
        currentRowCount++;
        updateDialogSize();
    }

    private void updateDialogSize() {
        Window window = SwingUtilities.getWindowAncestor(mainPanel);
        if (window != null) {
            int newHeight = Math.min(
                    Math.max(currentRowCount, MIN_ROWS),
                    MAX_ROWS
            ) * ROW_HEIGHT + 100; // 动态计算高度

            window.setSize(
                    window.getWidth(),
                    Math.min(newHeight, Toolkit.getDefaultToolkit().getScreenSize().height - 100)
            );
            window.validate();
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mainPanel;
    }

    public Map<String, String> getModifiedMappings() {
        Map<String, String> modified = new HashMap<>();
        for (MappingRow row : rows) {
            String dbType = row.dbTypeField.getText().trim().toLowerCase();
            if (!dbType.isEmpty()) {
                modified.put(dbType, row.javaTypeField.getText().trim());
            }
        }
        return modified;
    }

    private static class MappingRow {
        JTextField dbTypeField;
        JTextField javaTypeField;
        JPanel panel;

        MappingRow(String dbType, String javaType) {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            dbTypeField = new JTextField(dbType, 12);
            javaTypeField = new JTextField(javaType, 20);

            panel.add(new JLabel("DB类型:"));
            panel.add(dbTypeField);
            panel.add(new JLabel("→ Java类型:"));
            panel.add(javaTypeField);

            JButton removeBtn = new JButton("删除");
            removeBtn.addActionListener(e -> removeRow());
            panel.add(removeBtn);
        }

        private void removeRow() {
            Container parent = panel.getParent();
            parent.remove(panel);
            parent.revalidate();
            parent.repaint();
        }
    }
}
