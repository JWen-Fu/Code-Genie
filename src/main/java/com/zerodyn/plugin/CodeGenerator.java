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
    private final String basePath; // 项目根目录

    public CodeGenerator(String basePath) {
        this.basePath = basePath;
    }

    public void generateEntityClass(DDLParser.Table table) throws IOException {
        String code = generateEntityClassCode(table);
        writeToFile(table.getName() + "Entity.java", code);
    }

    public void generateControllerClass(DDLParser.Table table) throws IOException {
        String code = generateControllerClassCode(table);
        writeToFile(table.getName() + "Controller.java", code);
    }

    public void generateServiceClass(DDLParser.Table table) throws IOException {
        String code = generateServiceClassCode(table);
        writeToFile(table.getName() + "Service.java", code);
    }

    private void writeToFile(String fileName, String code) throws IOException {
        // 将文件生成到项目根目录下
        File file = new File(basePath, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(code);
        }
    }

    private String generateEntityClassCode(DDLParser.Table table) {
        StringBuilder code = new StringBuilder();
        code.append("public class ").append(table.getName()).append("Entity {\n");

        table.getColumns().forEach(column ->
                code.append("    private ").append(mapColumnType(column.getType()))
                        .append(" ").append(column.getName()).append(";\n")
        );
        code.append("\n    // Getters and Setters\n");
        table.getColumns().forEach(column -> {
            String capitalized = column.getName().substring(0, 1).toUpperCase() + column.getName().substring(1);
            code.append("    public ").append(mapColumnType(column.getType())).append(" get")
                    .append(capitalized).append("() { return ").append(column.getName()).append("; }\n");
            code.append("    public void set").append(capitalized).append("(")
                    .append(mapColumnType(column.getType())).append(" ").append(column.getName())
                    .append(") { this.").append(column.getName()).append(" = ").append(column.getName()).append("; }\n");
        });
        code.append("}\n");
        return code.toString();
    }

    private String generateControllerClassCode(DDLParser.Table table) {
        return "public class " + table.getName() + "Controller {\n" +
                "    private " + table.getName() + "Service " + table.getName().toLowerCase() + "Service;\n\n" +
                "    public void getAll" + table.getName() + "() {\n" +
                "        // TODO: Implement get all functionality\n" +
                "    }\n" +
                "}\n";
    }

    private String generateServiceClassCode(DDLParser.Table table) {
        return "public class " + table.getName() + "Service {\n" +
                "    public void save" + table.getName() + "(" + table.getName() + "Entity entity) {\n" +
                "        // TODO: Implement save functionality\n" +
                "    }\n" +
                "}\n";
    }

    private String mapColumnType(String sqlType) {
        switch (sqlType.toLowerCase()) {
            case "int":
            case "int(11)":
                return "int";
            case "varchar(255)":
                return "String";
            case "date":
                return "java.util.Date";
            // 其它类型默认返回 String，可根据需要扩展
            default:
                return "String";
        }
    }
}
