package com.zerodyn.plugin.provider;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author JWen
 * @since 2025/4/6
 */
public class ResourceTemplateProvider implements TemplateProvider {
    private final String basePath;

    public ResourceTemplateProvider(String basePath) {
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
    }

    @Override
    public String getTemplateContent(String templateName) {
        try (InputStream is = getClass().getResourceAsStream(basePath + templateName)) {
            if (is == null) {
                throw new RuntimeException("Template not found in resources: " + templateName);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read template: " + templateName, e);
        }
    }

    @Override
    public boolean templateExists(String templateName) {
        return getClass().getResource(basePath + templateName) != null;
    }
}
