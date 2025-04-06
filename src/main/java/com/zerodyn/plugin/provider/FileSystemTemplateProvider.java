package com.zerodyn.plugin.provider;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author JWen
 * @since 2025/4/6
 */
public class FileSystemTemplateProvider implements TemplateProvider {
    private final Path templateDir;

    public FileSystemTemplateProvider(Path templateDir) {
        this.templateDir = templateDir;
    }

    @Override
    public String getTemplateContent(String templateName) {
        try {
            return Files.readString(templateDir.resolve(templateName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read template: " + templateName, e);
        }
    }

    @Override
    public boolean templateExists(String templateName) {
        return Files.exists(templateDir.resolve(templateName));
    }
}
