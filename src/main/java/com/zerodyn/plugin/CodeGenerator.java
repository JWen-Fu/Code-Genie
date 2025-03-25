package com.zerodyn.plugin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author JWen
 * @since 2025/3/24
 */
public class CodeGenerator {
    // Method to generate Entity class code
    public void generateEntityClass(DDLParser.Table table) throws IOException {
        String entityClassCode = generateEntityClassCode(table);
        writeToFile(table.getName() + "Entity.java", entityClassCode);
    }

    // Method to generate Controller class code
    public void generateControllerClass(DDLParser.Table table) throws IOException {
        String controllerClassCode = generateControllerClassCode(table);
        writeToFile(table.getName() + "Controller.java", controllerClassCode);
    }

    // Method to generate Service class code
    public void generateServiceClass(DDLParser.Table table) throws IOException {
        String serviceClassCode = generateServiceClassCode(table);
        writeToFile(table.getName() + "Service.java", serviceClassCode);
    }

    // Helper method to write generated code to a file
    private void writeToFile(String fileName, String code) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(code);
        }
    }

    // Generate Entity class code
    private String generateEntityClassCode(DDLParser.Table table) {
        StringBuilder code = new StringBuilder();
        code.append("public class ").append(table.getName()).append("Entity {\n");

        for (DDLParser.Column column : table.getColumns()) {
            code.append("    private ").append(mapColumnType(column.getType())).append(" ")
                    .append(column.getName()).append(";\n");
        }

        code.append("\n    // Getters and setters\n");
        for (DDLParser.Column column : table.getColumns()) {
            String capitalized = column.getName().substring(0, 1).toUpperCase() + column.getName().substring(1);
            code.append("    public ").append(mapColumnType(column.getType())).append(" get")
                    .append(capitalized).append("() { return ").append(column.getName()).append("; }\n");

            code.append("    public void set").append(capitalized).append("(")
                    .append(mapColumnType(column.getType())).append(" ").append(column.getName())
                    .append(") { this.").append(column.getName()).append(" = ").append(column.getName()).append("; }\n");
        }

        code.append("}\n");
        return code.toString();
    }

    // Generate Controller class code
    private String generateControllerClassCode(DDLParser.Table table) {
        return "public class " + table.getName() + "Controller {\n" +
                "    private " + table.getName() + "Service " + table.getName().toLowerCase() + "Service;\n\n" +
                "    public void getAll" + table.getName() + "() {\n" +
                "        // Implement get all functionality\n" +
                "    }\n" +
                "}\n";
    }

    // Generate Service class code
    private String generateServiceClassCode(DDLParser.Table table) {
        return "public class " + table.getName() + "Service {\n" +
                "    public void save" + table.getName() + "(" + table.getName() + "Entity entity) {\n" +
                "        // Implement save functionality\n" +
                "    }\n" +
                "}\n";
    }

    // Helper method to map column types to Java types
    private String mapColumnType(String sqlType) {
        switch (sqlType.toLowerCase()) {
            case "int":
                return "int";
            case "varchar":
                return "String";
            case "date":
                return "java.util.Date";
            default:
                return "String"; // Default type
        }
    }
}
