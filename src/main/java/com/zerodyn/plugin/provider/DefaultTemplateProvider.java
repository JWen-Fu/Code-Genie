package com.zerodyn.plugin.provider;

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
        initDefaultTemplates();
    }

    private void initDefaultTemplates() {
        // 实体模板
        defaultTemplates.put("EntityTemplate.ftl",
                "<#if useLombok>\n" +
                        "import lombok.Data;\n" +
                        "import lombok.NoArgsConstructor;\n" +
                        "import lombok.AllArgsConstructor;\n" +
                        "</#if>\n\n" +
                        "<#if useLombok>\n" +
                        "@Data\n" +
                        "@NoArgsConstructor\n" +
                        "@AllArgsConstructor\n" +
                        "</#if>\n" +
                        "public class ${className}${componentType} {\n" +
                        "<#list table.columns as column>\n" +
                        "    private ${typeMapper.getJavaType(column.type)} ${column.name};\n" +
                        "</#list>\n" +
                        "}");

        // 其他模板...
        // 确保包含所有getRequiredTemplates()中列出的模板
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