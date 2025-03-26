package com.zerodyn.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author JWen
 * @since 2025/3/24
 */
public class DDLParser {
    /**
     * 解析 CREATE TABLE 语句，支持带有额外参数（如 ENGINE、CHARSET、COMMENT 等）的DDL
     *
     * @param ddl DDL 语句
     * @return Table 对象，包含表名和所有解析到的列；解析失败返回 null
     */
    public Table parseDDL(String ddl) {
        // 移除注释和多余空格
        String normalizedDDL = ddl.replaceAll("/\\*.*?\\*/", "")
                .replaceAll("--.*?\\n", "")
                .replaceAll("\\s+", " ");

        Pattern tablePattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+(?:IF NOT EXISTS\\s+)?`?(\\w+)`?\\s*\\(([^;]+)\\)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = tablePattern.matcher(normalizedDDL);

        if (matcher.find()) {
            String tableName = matcher.group(1);
            String columnsPart = matcher.group(2);
            return new Table(tableName, parseColumns(columnsPart));
        }
        return null;
    }

    private List<Column> parseColumns(String columnsDDL) {
        List<Column> columns = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "`?(\\w+)`?\\s+([a-z]+\\([\\d,\\s]+\\)|[a-z]+)\\b",
                Pattern.CASE_INSENSITIVE);

        String[] columnDefs = columnsDDL.split(",(?![^(]*\\))");

        for (String def : columnDefs) {
            def = def.trim();
            if (def.startsWith("PRIMARY KEY") || def.startsWith("INDEX") || def.startsWith("UNIQUE")) {
                continue;
            }

            Matcher matcher = pattern.matcher(def);
            if (matcher.find()) {
                String columnName = snakeToCamel(matcher.group(1)); // 转换命名格式
                String columnType = matcher.group(2).toUpperCase();
                columns.add(new Column(columnName, columnType));
            }
        }
        return columns;
    }

    /**
     * 蛇形命名转小驼峰命名（snake_case -> camelCase）
     * 示例：
     *   "user_name" -> "userName"
     *   "total_price" -> "totalPrice"
     */
    private String snakeToCamel(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < snakeCase.length(); i++) {
            char currentChar = snakeCase.charAt(i);

            if (currentChar == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(currentChar));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(currentChar));
                }
            }
        }

        return result.toString();
    }

    public static class Table {
        private final String name;
        private final List<Column> columns;

        public Table(String name, List<Column> columns) {
            this.name = name;
            this.columns = columns;
        }

        public String getName() {
            return name;
        }

        public List<Column> getColumns() {
            return columns;
        }
    }

    public static class Column {
        private final String name;
        private final String type;

        public Column(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}