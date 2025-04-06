package com.zerodyn.plugin.provider;

import freemarker.template.TemplateException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JWen
 * @since 2025/4/6
 */
public class CompositeTemplateProvider implements TemplateProvider {
    private final List<TemplateProvider> providers;
    private final TemplateProvider fallbackProvider;

    public CompositeTemplateProvider(List<TemplateProvider> providers,
                                     TemplateProvider fallbackProvider) {
        this.providers = new ArrayList<>(providers);
        this.fallbackProvider = fallbackProvider;
    }

    @Override
    public String getTemplateContent(String templateName) {
        for (TemplateProvider provider : providers) {
            if (provider.templateExists(templateName)) {
                return provider.getTemplateContent(templateName);
            }
        }
        return fallbackProvider.getTemplateContent(templateName);
    }

    @Override
    public boolean templateExists(String templateName) {
        return providers.stream().anyMatch(p -> p.templateExists(templateName)) ||
                fallbackProvider.templateExists(templateName);
    }
}
