package com.zerodyn.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author JWen
 * @since 2025/3/25
 */
public class FieldTypeMapper {
    private static final String CONFIG_DIR = ".codegen";
    private static final String CONFIG_FILE = "field-mappings.properties";
    private final Map<String, String> mappings = new HashMap<>();

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
        saveToFile();
    }

    private void saveToFile() throws IOException {
        File configDir = new File(System.getProperty("user.home"), CONFIG_DIR);
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new IOException("Failed to create config directory");
        }

        try (OutputStream os = new FileOutputStream(getConfigFile())) {
            Properties props = new Properties();
            mappings.forEach(props::setProperty);
            props.store(os, "Field Type Mappings");
        }
    }

    private File getConfigFile() {
        return new File(System.getProperty("user.home"), CONFIG_DIR + File.separator + CONFIG_FILE);
    }

    private void initDefaultMappings() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("varchar", "String");
        defaults.put("char", "String");
        defaults.put("text", "String");
        defaults.put("int", "Integer");
        defaults.put("integer", "Integer");
        defaults.put("tinyint", "Integer");
        defaults.put("decimal", "java.math.BigDecimal");
        defaults.put("float", "Float");
        defaults.put("double", "Double");
        defaults.put("date", "java.time.LocalDate");
        defaults.put("datetime", "java.time.LocalDateTime");
        mappings.putAll(defaults);
    }

    public String getJavaType(String sqlType) {
        String baseType = sqlType.split("\\(")[0].toLowerCase();
        return mappings.getOrDefault(baseType, "Object");
    }

    public Map<String, String> getAllMappings() {
        return new HashMap<>(mappings);
    }

    public Map<String, String> getRelevantMappings(Set<String> requiredTypes) {
        return requiredTypes.stream()
                .collect(Collectors.toMap(
                        type -> type,
                        type -> mappings.getOrDefault(type, "Object"))
                );
    }
}
