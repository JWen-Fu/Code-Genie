package com.zerodyn.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author JWen
 * @since 2025/3/24
 */
public class CodeGenerator {
    private final String basePath;
    private final FieldTypeMapper fieldTypeMapper;

    public CodeGenerator(String basePath, FieldTypeMapper fieldTypeMapper) {
        this.basePath = basePath;
        this.fieldTypeMapper = fieldTypeMapper;
    }

    // Generate Entity class
    public void generateEntityClass(DDLParser.Table table) throws IOException {
        String code = generateEntityClassCode(table);
        writeToFile(toCamelCase(table.getName()) + "Entity.java", code);
    }

    // Generate Controller class
    public void generateControllerClass(DDLParser.Table table) throws IOException {
        String code = generateControllerClassCode(table);
        writeToFile(toCamelCase(table.getName()) + "Controller.java", code);
    }

    // Generate Service class
    public void generateServiceClass(DDLParser.Table table) throws IOException {
        String code = generateServiceClassCode(table);
        writeToFile(toCamelCase(table.getName()) + "Service.java", code);
    }

    // Convert table name to camel case (e.g., "t_user" -> "TUser")
    private String toCamelCase(String name) {
        String[] parts = name.split("_");
        StringBuilder camelCaseName = new StringBuilder();
        for (String part : parts) {
            camelCaseName.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
        }
        return camelCaseName.toString();
    }

    // Write code to a file
    private void writeToFile(String fileName, String code) throws IOException {
        File file = new File(basePath, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(code);
        }
    }

    // Generate Entity class code dynamically based on parsed columns
    private String generateEntityClassCode(DDLParser.Table table) {
        StringBuilder code = new StringBuilder();
        code.append("public class ").append(toCamelCase(table.getName())).append("Entity {\n");

        // Dynamically generate fields based on DDL columns
        for (DDLParser.Column column : table.getColumns()) {
            String javaType = fieldTypeMapper.getJavaType(column.getType());
            code.append("    private ").append(javaType).append(" ").append(column.getName()).append(";\n");
        }

        code.append("\n    // Getters and Setters\n");
        for (DDLParser.Column column : table.getColumns()) {
            String capitalized = column.getName().substring(0, 1).toUpperCase() + column.getName().substring(1);
            code.append("    public ").append(fieldTypeMapper.getJavaType(column.getType()))
                    .append(" get").append(capitalized).append("() { return ").append(column.getName()).append("; }\n");
            code.append("    public void set").append(capitalized).append("(")
                    .append(fieldTypeMapper.getJavaType(column.getType())).append(" ").append(column.getName())
                    .append(") { this.").append(column.getName()).append(" = ").append(column.getName()).append("; }\n");
        }
        code.append("}\n");
        return code.toString();
    }

    private String generateControllerClassCode(DDLParser.Table table) {
        return "public class " + toCamelCase(table.getName()) + "Controller {\n" +
                "    private " + toCamelCase(table.getName()) + "Service " + toCamelCase(table.getName()).toLowerCase() + "Service;\n\n" +
                "    public void getAll" + toCamelCase(table.getName()) + "() {\n" +
                "        // TODO: Implement get all functionality\n" +
                "    }\n" +
                "}\n";
    }

    private String generateServiceClassCode(DDLParser.Table table) {
        return "public class " + toCamelCase(table.getName()) + "Service {\n" +
                "    public void save" + toCamelCase(table.getName()) + "(" + toCamelCase(table.getName()) + "Entity entity) {\n" +
                "        // TODO: Implement save functionality\n" +
                "    }\n" +
                "}\n";
    }
}
