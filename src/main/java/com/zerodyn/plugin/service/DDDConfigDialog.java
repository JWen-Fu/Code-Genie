package com.zerodyn.plugin.service;

import com.intellij.openapi.ui.DialogWrapper;
import com.zerodyn.plugin.config.ComponentConfig;
import com.zerodyn.plugin.config.DDDConfiguration;
import com.zerodyn.plugin.config.LayerConfig;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.Map;

/**
 * @author JWen
 * @since 2025/4/5
 */
public class DDDConfigDialog extends DialogWrapper {
    private final DDDConfiguration config;
    private final JTabbedPane tabbedPane = new JTabbedPane();

    public DDDConfigDialog(DDDConfiguration initialConfig) {
        super(true);
        this.config = initialConfig;
        initUI();
        init();
    }

    private void initUI() {
        // 领域层配置
        tabbedPane.addTab("领域层", createLayerPanel("domain"));

        // 应用层配置
        tabbedPane.addTab("应用层", createLayerPanel("application"));

        // 基础设施层配置
        tabbedPane.addTab("基础设施层", createLayerPanel("infrastructure"));

        // 新增接口层配置
        tabbedPane.addTab("接口层", createLayerPanel("interfaces"));

        // 高级配置
        JPanel advancedPanel = new JPanel(new BorderLayout());
        JCheckBox cqrsCheckBox = new JCheckBox("启用CQRS模式", config.isEnableCQRS());
        cqrsCheckBox.addChangeListener(e ->
                config.setEnableCQRS(cqrsCheckBox.isSelected()));
        advancedPanel.add(cqrsCheckBox, BorderLayout.NORTH);
        tabbedPane.addTab("高级", advancedPanel);

        setTitle("DDD架构配置");
        getContentPane().add(tabbedPane);
        setSize(600, 400);
    }

    private JPanel createLayerPanel(String layerKey) {
        LayerConfig layer = config.getLayers().get(layerKey);
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (Map.Entry<String, ComponentConfig> entry : layer.getComponents().entrySet()) {
            if ("ValueObject".equals(entry.getKey())) {
                continue;
            }
            String componentType = entry.getKey();
            ComponentConfig compConfig = entry.getValue();

            // 组件类型标签
            panel.add(new JLabel(componentType + "包路径:"));

            // 包路径输入框
            JTextField packageField = new JTextField(compConfig.getBasePackage(), 20);
            packageField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) { updateConfig(); }
                public void insertUpdate(DocumentEvent e) { updateConfig(); }
                public void removeUpdate(DocumentEvent e) { updateConfig(); }

                private void updateConfig() {
                    compConfig.setBasePackage(packageField.getText().trim());
                }
            });
            panel.add(packageField);

            // 模板选择器
            panel.add(new JLabel(componentType + "模板:"));
            JTextField templateField = new JTextField(compConfig.getTemplateFile(), 15);
            JButton browseButton = new JButton("浏览...");
            browseButton.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    templateField.setText(file.getName());
                    compConfig.setTemplateFile(file.getName());
                }
            });
            JPanel templatePanel = new JPanel(new BorderLayout(5, 0));
            templatePanel.add(templateField, BorderLayout.CENTER);
            templatePanel.add(browseButton, BorderLayout.EAST);
            panel.add(templatePanel);
        }
        return panel;
    }

    public DDDConfiguration getConfiguration() {
        return config;
    }

    @Override
    protected JComponent createCenterPanel() {
        return tabbedPane;
    }
}
