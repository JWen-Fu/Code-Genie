package com.zerodyn.plugin.template;

import com.zerodyn.plugin.provider.TemplateProvider;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JWen
 * @since 2025/4/5
 */
public class TemplateManager {
    private final Configuration fmConfig;
    private final TemplateProvider templateProvider;
    private final TemplateCache templateCache;

    public TemplateManager(TemplateProvider templateProvider) {
        this.templateProvider = templateProvider;
        this.fmConfig = createConfiguration();
        this.templateCache = new TemplateCache();
    }

    private Configuration createConfiguration() {
        Configuration config = new Configuration(Configuration.VERSION_2_3_31);
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return config;
    }

    public Template getTemplate(String templateName) throws TemplateException {
        // 先从缓存获取
        Template template = templateCache.get(templateName);
        if (template != null) {
            return template;
        }

        // 从提供器获取内容并编译模板
        try {
            String templateContent = templateProvider.getTemplateContent(templateName);
            template = new Template(templateName, templateContent, fmConfig);
            templateCache.put(templateName, template);
            return template;
        } catch (IOException e) {
            throw new RuntimeException("Failed to compile template: " + templateName);
        }
    }

    // 简单的模板缓存实现
    private static class TemplateCache {
        private final Map<String, Template> cache = new ConcurrentHashMap<>();

        public Template get(String templateName) {
            return cache.get(templateName);
        }

        public void put(String templateName, Template template) {
            cache.put(templateName, template);
        }
    }
}