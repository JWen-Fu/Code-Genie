package com.zerodyn.plugin.service;

import com.intellij.openapi.ui.DialogWrapper;
import com.zerodyn.plugin.config.ComponentConfig;
import com.zerodyn.plugin.config.DDDConfiguration;
import com.zerodyn.plugin.config.LayerConfig;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
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

        // 接口层配置
        JPanel interfacesPanel = createLayerPanel("interfaces");
        // 添加Controller特殊配置
        addControllerSpecificConfig(interfacesPanel, "interfaces");
        tabbedPane.addTab("接口层", interfacesPanel);

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
        LayerConfig layer = config.getLayer(layerKey);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (Map.Entry<String, ComponentConfig> entry : layer.getComponents().entrySet()) {
            String componentType = entry.getKey();
            ComponentConfig compConfig = entry.getValue();

            // 组件配置面板
            JPanel compPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            compPanel.setBorder(BorderFactory.createTitledBorder(componentType + "配置"));

            // 包路径配置
            compPanel.add(new JLabel("包路径:"));
            JTextField packageField = new JTextField(compConfig.getBasePackage(), 20);
            packageField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
                compConfig.setBasePackage(packageField.getText().trim());
            }));
            compPanel.add(packageField);

            // 模板文件配置
            compPanel.add(new JLabel("模板文件:"));
            JPanel templatePanel = new JPanel(new BorderLayout(5, 0));
            JTextField templateField = new JTextField(compConfig.getTemplateFile(), 15);
            JButton browseButton = new JButton("浏览...");
            browseButton.addActionListener(e -> browseTemplateFile(templateField, compConfig));
            templatePanel.add(templateField, BorderLayout.CENTER);
            templatePanel.add(browseButton, BorderLayout.EAST);
            compPanel.add(templatePanel);

            panel.add(compPanel);
        }

        return panel;
    }

    private void addControllerSpecificConfig(JPanel panel, String layerKey) {
        LayerConfig layer = config.getLayer(layerKey);
        if (!layer.hasComponent("Controller")) {
            layer.addComponent("Controller", "interfaces.rest", "ControllerTemplate.ftl");
        }

        ComponentConfig controllerConfig = layer.getComponents().get("Controller");

        // API前缀配置
        JPanel apiPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        apiPanel.setBorder(BorderFactory.createTitledBorder("Controller特殊配置"));

        apiPanel.add(new JLabel("API前缀:"));
        JTextField apiPrefixField = new JTextField(
                controllerConfig.getAdditionalConfig().getOrDefault("apiPrefix", "/api"),
                20
        );
        apiPrefixField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            controllerConfig.addConfigItem("apiPrefix", apiPrefixField.getText().trim());
        }));
        apiPanel.add(apiPrefixField);

        panel.add(apiPanel);
    }

    private void browseTemplateFile(JTextField templateField, ComponentConfig config) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("FreeMarker模板文件", "ftl"));
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            templateField.setText(file.getName());
            config.setTemplateFile(file.getName());
        }
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;

        SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }

        @Override public void insertUpdate(DocumentEvent e) { callback.run(); }
        @Override public void removeUpdate(DocumentEvent e) { callback.run(); }
        @Override public void changedUpdate(DocumentEvent e) { callback.run(); }
    }

    public DDDConfiguration getConfiguration() {
        return config;
    }

    @Override
    protected JComponent createCenterPanel() {
        return tabbedPane;
    }
}
