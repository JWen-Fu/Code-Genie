package com.zerodyn.plugin.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        this.fallbackProvider = Objects.requireNonNull(fallbackProvider,
                "Fallback provider cannot be null");
    }

    @Override
    public String getTemplateContent(String templateName) {
        // 先尝试所有提供器
        for (TemplateProvider provider : providers) {
            try {
                if (provider.templateExists(templateName)) {
                    return provider.getTemplateContent(templateName);
                }
            } catch (Exception e) {
                // 单个提供器失败不影响整体流程
                continue;
            }
        }

        // 全部失败后使用回退提供器
        try {
            return fallbackProvider.getTemplateContent(templateName);
        } catch (Exception e) {
            throw new RuntimeException("All template providers failed, including fallback", e);
        }
    }

    @Override
    public boolean templateExists(String templateName) {
        return providers.stream().anyMatch(p -> {
            try {
                return p.templateExists(templateName);
            } catch (Exception e) {
                return false;
            }
        }) || fallbackProvider.templateExists(templateName);
    }
}