/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin.provider;

import freemarker.template.TemplateException;

/**
 * @author JWen
 * @since 2025/4/6
 */
public interface TemplateProvider {
    /**
     * 获取模板内容
     * @param templateName 模板名称
     * @return 模板内容
     * @throws TemplateException 模板异常
     */
    String getTemplateContent(String templateName);

    /**
     * 检查模板是否存在
     * @param templateName 模板名称
     * @return 是否存在
     */
    boolean templateExists(String templateName);
}