package com.zerodyn.plugin;

import java.io.IOException;

/**
 * @author JWen
 * @since 2025/3/24
 */
public class Main {
    public static void main(String[] args) {
        // Example DDL for parsing
        String ddl = """
                CREATE TABLE `User` (
                  `id` int(11) NOT NULL,
                  `name` varchar(255) DEFAULT NULL,
                  `email` varchar(255) DEFAULT NULL,
                  PRIMARY KEY (`id`)
                );""";

        DDLParser ddlParser = new DDLParser();
        DDLParser.Table table = ddlParser.parseDDL(ddl);

        if (table != null) {
            CodeGenerator codeGenerator = new CodeGenerator();
            try {
                codeGenerator.generateEntityClass(table);
                codeGenerator.generateControllerClass(table);
                codeGenerator.generateServiceClass(table);
                System.out.println("Code generation completed successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to parse DDL.");
        }
    }
}
