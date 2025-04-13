/*
 * Copyright (c) by Zerodyn Technologies 2025-2025. All rights reserved.
 */

package com.zerodyn.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author JWen
 * @since 2025/3/25
 */
public class FieldTypeMapper {
    private static final String CONFIG_DIR = ".codegen";
    private static final String CONFIG_FILE = "field-mappings.properties";
    private final Map<String, String> mappings = new LinkedHashMap<>();
    private static final Pattern TYPE_PATTERN =
            Pattern.compile("^(\\w+)(?:\\(.*\\))?(?:\\s+UNSIGNED)?$", Pattern.CASE_INSENSITIVE);

    public FieldTypeMapper() {
        loadMappings();
    }

    private void loadMappings() {
        File configFile = getConfigFile();
        if (!configFile.exists()) {
            initDefaultMappings();
            return;
        }

        try (InputStream is = new FileInputStream(configFile)) {
            Properties props = new Properties();
            props.load(is);
            props.forEach((k, v) -> mappings.put(k.toString().toLowerCase(), v.toString()));
        } catch (IOException e) {
            initDefaultMappings();
        }
    }

    public void savePartialMappings(Map<String, String> partialMappings) throws IOException {
        partialMappings.forEach((k, v) -> mappings.put(k.toLowerCase(), v));

        File configDir = new File(System.getProperty("user.home"), CONFIG_DIR);
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new IOException("无法创建配置目录");
        }

        try (OutputStream os = new FileOutputStream(getConfigFile())) {
            Properties props = new Properties();
            mappings.forEach(props::setProperty);
            props.store(os, "Field Type Mappings");
        }
    }

    public Map<String, String> getRelevantMappings(Set<String> requiredTypes) {
        Map<String, String> result = new LinkedHashMap<>();
        requiredTypes.forEach(type -> {
            String javaType = mappings.getOrDefault(type, "Object");
            result.put(type, javaType);
        });
        return result;
    }

    private File getConfigFile() {
        return new File(System.getProperty("user.home"),
                CONFIG_DIR + File.separator + CONFIG_FILE);
    }

    private void initDefaultMappings() {
        Map<String, String> defaults = new HashMap<>();
        // 整数类型
        defaults.put("tinyint", "Integer");
        defaults.put("smallint", "Integer");
        defaults.put("mediumint", "Integer");
        defaults.put("int", "Integer");
        defaults.put("integer", "Integer");
        defaults.put("bigint", "Long");
        // 布尔类型
        defaults.put("boolean", "Boolean");
        // 字符串类型
        defaults.put("varchar", "String");
        defaults.put("char", "String");
        defaults.put("text", "String");
        // 小数类型
        defaults.put("decimal", "java.math.BigDecimal");
        defaults.put("numeric", "java.math.BigDecimal");
        defaults.put("float", "Float");
        defaults.put("double", "Double");
        // 日期时间
        defaults.put("date", "java.time.LocalDate");
        defaults.put("time", "java.time.LocalTime");
        defaults.put("datetime", "java.time.LocalDateTime");
        defaults.put("timestamp", "java.time.Instant");

        mappings.putAll(defaults);
    }
}
