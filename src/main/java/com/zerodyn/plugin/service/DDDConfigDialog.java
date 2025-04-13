/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.zerodyn.plugin.config.ComponentConfig;
import com.zerodyn.plugin.config.DDDConfiguration;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author JWen
 * @since 2025/4/5
 */
@Slf4j
public class DDDConfigDialog extends DialogWrapper {
    private final DDDConfiguration config;
    private final Project project;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final Map<String, Map<String, JTextField>> componentFields = new HashMap<>();

    public DDDConfigDialog(Project project, DDDConfiguration initialConfig) {
        super(project, true);
        this.project = Objects.requireNonNull(project);
        this.config = Objects.requireNonNull(initialConfig);
        setTitle("DDD架构配置");
        init();
    }

    @Override
    protected void init() {
        super.init();
        initUI();
    }

    private void initUI() {
        List<String> modules = getProjectModules();
        if (modules.isEmpty()) {
            modules.add("");
        }

        // 初始化各层配置
        initLayerTab("domain", modules);
        initLayerTab("application", modules);
        initLayerTab("infrastructure", modules);
        initLayerTab("interfaces", modules);

        // 高级配置
        initAdvancedTab();
    }

    private void initLayerTab(String layer, List<String> modules) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 模块选择
        JPanel modulePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modulePanel.add(new JLabel("目标模块:"));
        JComboBox<String> moduleCombo = new JComboBox<>(modules.toArray(new String[0]));
        moduleCombo.setSelectedItem(config.getLayer(layer).getModuleName());
        moduleCombo.addActionListener(e ->
                config.getLayer(layer).setModuleName((String) moduleCombo.getSelectedItem())
        );
        modulePanel.add(moduleCombo);
        contentPanel.add(modulePanel);

        // 组件配置（过滤掉ValueObject）
        Map<String, ComponentConfig> components = config.getLayer(layer).getComponents();
        Map<String, JTextField> layerFields = new HashMap<>();
        componentFields.put(layer, layerFields);

        components.entrySet().stream()
                .filter(entry -> !"ValueObject".equals(entry.getKey()))
                .forEach(entry -> {
                    String compType = entry.getKey();
                    ComponentConfig compConfig = entry.getValue();

                    JPanel compPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                    compPanel.setBorder(BorderFactory.createTitledBorder(compType));

                    // 包路径配置
                    compPanel.add(new JLabel("包路径:"));
                    JTextField pkgField = new JTextField(compConfig.getBasePackage());
                    pkgField.getDocument().addDocumentListener(new ConfigUpdater(compConfig, "basePackage", pkgField));
                    compPanel.add(pkgField);
                    layerFields.put(compType + ".pkg", pkgField);

                    // 模板文件配置
                    compPanel.add(new JLabel("模板文件:"));
                    JPanel templatePanel = new JPanel(new BorderLayout(5, 0));
                    JTextField templateField = new JTextField(compConfig.getTemplateFile());
                    JButton browseBtn = new JButton("浏览...");
                    browseBtn.addActionListener(e -> browseTemplateFile(templateField));
                    templatePanel.add(templateField, BorderLayout.CENTER);
                    templatePanel.add(browseBtn, BorderLayout.EAST);
                    compPanel.add(templatePanel);
                    layerFields.put(compType + ".template", templateField);
                    templateField.getDocument().addDocumentListener(new ConfigUpdater(compConfig, "templateFile", templateField));

                    contentPanel.add(compPanel);
                });

        panel.add(contentPanel, BorderLayout.NORTH);
        tabbedPane.addTab(layer + "层", panel);
    }

    private void initAdvancedTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JCheckBox cqrsCheckBox = new JCheckBox("启用CQRS模式", config.isEnableCQRS());
        cqrsCheckBox.addChangeListener(e -> config.setEnableCQRS(cqrsCheckBox.isSelected()));
        panel.add(cqrsCheckBox, BorderLayout.NORTH);
        tabbedPane.addTab("高级", panel);
    }

    private void browseTemplateFile(JTextField targetField) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            targetField.setText(chooser.getSelectedFile().getName());
        }
    }

    private List<String> getProjectModules() {
        try {
            VirtualFile baseDir = project.getBaseDir();
            if (baseDir != null) {
                return Arrays.stream(baseDir.getChildren())
                        .filter(VirtualFile::isDirectory)
                        .filter(dir -> dir.findChild("src") != null)
                        .map(VirtualFile::getName)
                        .toList();
            }
        } catch (Exception e) {
            log.error("getProjectModules error", e);
        }
        return Collections.emptyList();
    }

    @Override
    protected JComponent createCenterPanel() {
        return tabbedPane;
    }

    public DDDConfiguration getConfiguration() {
        return config;
    }

    private record ConfigUpdater(ComponentConfig config, String field,
                                 JTextField textField) implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            switch (field) {
                case "basePackage":
                    config.setBasePackage(textField.getText());
                    break;
                case "templateFile":
                    config.setTemplateFile(textField.getText());
                    break;
            }
        }
    }
}
