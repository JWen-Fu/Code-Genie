package com.zerodyn.plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JWen
 * @since 2025/3/25
 */
public class FieldTypeMapper {
    // 默认数据库字段类型与Java类型映射
    private final Map<String, String> fieldTypeMapping;

    public FieldTypeMapper() {
        fieldTypeMapping = new HashMap<>();
        // Default mappings
        fieldTypeMapping.put("int", "int");
        fieldTypeMapping.put("tinyint", "int");
        fieldTypeMapping.put("varchar", "String");
        fieldTypeMapping.put("decimal", "java.math.BigDecimal");
        fieldTypeMapping.put("float", "float");
        fieldTypeMapping.put("double", "double");
        fieldTypeMapping.put("datetime", "java.util.Date");
        fieldTypeMapping.put("date", "java.util.Date");
        fieldTypeMapping.put("text", "String");
    }

    // Get Java type based on SQL type
    public String getJavaType(String sqlType) {
        sqlType = sqlType.toLowerCase().trim();
        return fieldTypeMapping.getOrDefault(sqlType, "String");
    }

    // Allow user to add custom mappings
    public void addMapping(String sqlType, String javaType) {
        fieldTypeMapping.put(sqlType.toLowerCase(), javaType);
    }
}
