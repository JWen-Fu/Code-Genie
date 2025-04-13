/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin.template;

import com.zerodyn.plugin.provider.CompositeTemplateProvider;
import com.zerodyn.plugin.provider.DefaultTemplateProvider;
import com.zerodyn.plugin.provider.FileSystemTemplateProvider;
import com.zerodyn.plugin.provider.ResourceTemplateProvider;
import com.zerodyn.plugin.provider.TemplateProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author JWen
 * @since 2025/4/6
 */
public class TemplateInitializer {
    private static final String USER_TEMPLATE_DIR = ".codegen/templates/ddd";
    private static final String RESOURCE_TEMPLATE_DIR = "/templates/ddd/";

    public static TemplateManager createDefaultTemplateManager() {
        // 1. 创建提供器链（顺序决定了优先级）
        List<TemplateProvider> providers = new ArrayList<>();

        // 用户自定义模板优先
        providers.add(new FileSystemTemplateProvider(getUserTemplateDir()));

        // 然后是内置资源模板
        providers.add(new ResourceTemplateProvider(RESOURCE_TEMPLATE_DIR));

        // 2. 创建复合提供器（使用DefaultTemplateProvider作为最终回退）
        CompositeTemplateProvider compositeProvider = new CompositeTemplateProvider(
                providers,
                new DefaultTemplateProvider()
        );

        // 3. 确保默认模板存在
        ensureDefaultTemplates();

        // 4. 初始化模板管理器
        return new TemplateManager(compositeProvider);
    }

    private static Path getUserTemplateDir() {
        Path dir = Paths.get(System.getProperty("user.home"), USER_TEMPLATE_DIR.split("/"));
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            return dir;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create template directory", e);
        }
    }

    public static void ensureDefaultTemplates() {
        Path userDir = getUserTemplateDir();
        ResourceTemplateProvider resourceProvider = new ResourceTemplateProvider(RESOURCE_TEMPLATE_DIR);
        DefaultTemplateProvider defaultProvider = new DefaultTemplateProvider();

        for (String template : getRequiredTemplates()) {
            Path templatePath = userDir.resolve(template);
            if (!Files.exists(templatePath)) {
                try {
                    // 尝试从资源复制
                    String content = resourceProvider.templateExists(template)
                            ? resourceProvider.getTemplateContent(template)
                            : defaultProvider.getTemplateContent(template);
                    Files.writeString(templatePath, content);
                } catch (IOException e) {
                    // 忽略错误，CompositeTemplateProvider会处理回退
                }
            }
        }
    }

    private static List<String> getRequiredTemplates() {
        return Arrays.asList(
                "EntityTemplate.ftl",
                "RepositoryTemplate.ftl",
                "ServiceTemplate.ftl",
                "DtoTemplate.ftl",
                "ControllerTemplate.ftl",
                "RepositoryImplTemplate.ftl"
        );
    }
}