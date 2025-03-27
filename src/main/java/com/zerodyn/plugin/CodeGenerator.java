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
    private final FieldTypeMapper typeMapper;
    private final boolean useLombok;

    public CodeGenerator(String basePath, FieldTypeMapper typeMapper, boolean useLombok) {
        this.basePath = basePath;
        this.typeMapper = typeMapper;
        this.useLombok = useLombok;
    }

    public void generateAll(DDLParser.Table table) throws IOException {
        generateEntityClass(table);
        generateServiceClass(table);
        generateControllerClass(table);
    }

    public void generateEntityClass(DDLParser.Table table) throws IOException {
        String code = buildEntityCode(table);
        writeFile(toCamelCase(table.getName()) + "Entity.java", code);
    }

    public void generateServiceClass(DDLParser.Table table) throws IOException {
        String code = buildServiceCode(table);
        writeFile(toCamelCase(table.getName()) + "Service.java", code);
    }

    public void generateControllerClass(DDLParser.Table table) throws IOException {
        String code = buildControllerCode(table);
        writeFile(toCamelCase(table.getName()) + "Controller.java", code);
    }

    private String buildEntityCode(DDLParser.Table table) {
        StringBuilder code = new StringBuilder();
        code.append("package com.example.entity;\n\n");
        code.append("import javax.persistence.*;\n");
        if (useLombok) code.append("import lombok.*;\n\n");

        if (!table.getComment().isEmpty()) {
            code.append("/**\n * ").append(table.getComment()).append("\n */\n");
        }

        code.append("@Entity\n");
        code.append("@Table(name = \"").append(table.getName()).append("\")\n");
        if (useLombok) {
            code.append("@Data\n@NoArgsConstructor\n@AllArgsConstructor\n");
        }
        code.append("public class ").append(toCamelCase(table.getName())).append("Entity {\n\n");

        for (DDLParser.Column column : table.getColumns()) {
            if (!column.getComment().isEmpty()) {
                code.append("    /** ").append(column.getComment()).append(" */\n");
            }

            code.append("    @Column(name = \"").append(column.getOriginalName()).append("\"");
            if (column.isNotNull()) code.append(", nullable = false");
            code.append(")\n");

            code.append("    private ").append(typeMapper.getJavaType(column.getType())).append(" ").append(column.getName()).append(";\n\n");
        }

        if (!useLombok) {
            code.append(generateGettersSetters(table));
        }

        code.append("}\n");
        return code.toString();
    }

    private String generateGettersSetters(DDLParser.Table table) {
        StringBuilder code = new StringBuilder();
        for (DDLParser.Column column : table.getColumns()) {
            String capitalized = column.getName().substring(0, 1).toUpperCase() + column.getName().substring(1);

            code.append("    public ").append(typeMapper.getJavaType(column.getType())).append(" get").append(capitalized).append("() {\n").append("        return this.").append(column.getName()).append(";\n").append("    }\n\n");

            code.append("    public void set").append(capitalized).append("(").append(typeMapper.getJavaType(column.getType())).append(" ").append(column.getName()).append(") {\n").append("        this.").append(column.getName()).append(" = ").append(column.getName()).append(";\n").append("    }\n\n");
        }
        return code.toString();
    }

    private String buildServiceCode(DDLParser.Table table) {
        return "package com.example.service;\n\n" + "import com.example.entity." + toCamelCase(table.getName()) + "Entity;\n" + "import org.springframework.stereotype.Service;\n\n" + "@Service\n" + "public class " + toCamelCase(table.getName()) + "Service {\n" + "    public void save(" + toCamelCase(table.getName()) + "Entity entity) {\n" + "        // TODO: Implement save logic\n" + "    }\n" + "}\n";
    }

    private String buildControllerCode(DDLParser.Table table) {
        return "package com.example.controller;\n\n" + "import com.example.entity." + toCamelCase(table.getName()) + "Entity;\n" + "import com.example.service." + toCamelCase(table.getName()) + "Service;\n" + "import org.springframework.web.bind.annotation.*;\n\n" + "@RestController\n" + "@RequestMapping(\"/api/" + table.getName().toLowerCase() + "\")\n" + "public class " + toCamelCase(table.getName()) + "Controller {\n" + "    private final " + toCamelCase(table.getName()) + "Service service;\n\n" + "    public " + toCamelCase(table.getName()) + "Controller(" + toCamelCase(table.getName()) + "Service service) {\n" + "        this.service = service;\n" + "    }\n\n" + "    @PostMapping\n" + "    public void create(@RequestBody " + toCamelCase(table.getName()) + "Entity entity) {\n" + "        service.save(entity);\n" + "    }\n" + "}\n";
    }

    private String toCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        for (String part : name.split("_")) {
            if (part.isEmpty()) continue;
            result.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
        }
        return result.toString();
    }

    private void writeFile(String fileName, String content) throws IOException {
        File dir = new File(basePath);
        if (!dir.exists()) dir.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir, fileName)))) {
            writer.write(content);
        }
    }
}
