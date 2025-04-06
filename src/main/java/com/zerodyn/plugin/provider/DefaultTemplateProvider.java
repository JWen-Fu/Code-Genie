package com.zerodyn.plugin.provider;

import freemarker.template.TemplateException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JWen
 * @since 2025/4/6
 */
public class DefaultTemplateProvider implements TemplateProvider {
    private final Map<String, String> defaultTemplates;

    public DefaultTemplateProvider() {
        this.defaultTemplates = new HashMap<>();
        // 可以配置化加载默认模板
        defaultTemplates.put("RepositoryTemplate.ftl",
                "package ${config.layers['domain'].components['Repository'].basePackage};\n\n" +
                        "public interface ${table.name}Repository {\n" +
                        "    ${table.name}Entity findById(String id);\n" +
                        "    void save(${table.name}Entity entity);\n" +
                        "}");
        // 其他默认模板...
    }

    @Override
    public String getTemplateContent(String templateName) {
        if (!defaultTemplates.containsKey(templateName)) {
            throw new RuntimeException("No default template available: " + templateName);
        }
        return defaultTemplates.get(templateName);
    }

    @Override
    public boolean templateExists(String templateName) {
        return defaultTemplates.containsKey(templateName);
    }
}
