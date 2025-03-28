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
    // 定义合法的SQL数据类型列表（防止误判key等关键字）
    private static final String SQL_DATA_TYPES =
            "int|integer|tinyint|smallint|mediumint|bigint|decimal|numeric|float|double|" +
                    "char|varchar|text|tinytext|mediumtext|longtext|blob|tinyblob|mediumblob|longblob|" +
                    "datetime|date|time|year|timestamp|boolean|bit|enum|set|json";

    public Table parseDDL(String ddl) {
        String normalizedDDL = ddl.replaceAll("/\\*.*?\\*/", "")
                .replaceAll("--.*?\\n", "")
                .replaceAll("\\s+", " ");

        Pattern tablePattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+(?:IF NOT EXISTS\\s+)?`?(\\w+)`?\\s*(?:COMMENT\\s+'([^']*)')?\\s*\\(([^;]+)\\)\\s*(?:COMMENT\\s*=\\s*'([^']*)')?",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = tablePattern.matcher(normalizedDDL);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String tableComment = matcher.group(2) != null ? matcher.group(2) :
                    matcher.group(4) != null ? matcher.group(4) : "";
            return new Table(tableName, parseColumns(matcher.group(3)), tableComment);
        }
        return null;
    }

    private List<Column> parseColumns(String columnsDDL) {
        List<Column> columns = new ArrayList<>();

        // 改进后的正则：确保只匹配合法的SQL数据类型
        Pattern pattern = Pattern.compile(
                "`?(\\w+)`?\\s+" +
                        "(" + SQL_DATA_TYPES + ")\\s*" + // 只匹配预定义的数据类型
                        "(?:\\([\\d,\\s]+\\))?\\s*" +   // 可选的长度定义
                        "(UNSIGNED\\s*)?" +
                        "(NOT NULL|NULL)?\\s*" +
                        "(DEFAULT\\s+[^,]+)?\\s*" +
                        "(?:COMMENT\\s+'([^']*)')?",
                Pattern.CASE_INSENSITIVE);

        String[] columnDefs = columnsDDL.split(",(?![^(]*\\))");
        for (String def : columnDefs) {
            def = def.trim();
            if (isConstraintDefinition(def)) continue;

            Matcher matcher = pattern.matcher(def);
            if (matcher.find()) {
                // 构建完整的类型字符串（包含长度定义）
                String fullType = matcher.group(2).toUpperCase();
                if (def.contains("(")) {
                    int start = def.indexOf("(");
                    int end = def.indexOf(")");
                    if (end > start) {
                        fullType += def.substring(start, end + 1);
                    }
                }

                columns.add(new Column(
                        matcher.group(1), // originalName
                        snakeToCamel(matcher.group(1)), // camelCaseName
                        fullType, // 完整的类型定义
                        !"NULL".equalsIgnoreCase(matcher.group(4)), // notNull
                        matcher.group(6) != null ? matcher.group(6) : "" // comment
                ));
            }
        }
        return columns;
    }

    // 判断是否是约束定义（增强版）
    private boolean isConstraintDefinition(String definition) {
        return definition.matches("(?i)^\\s*(PRIMARY\\s+KEY|UNIQUE\\s+(?:KEY|INDEX)?|" +
                "FOREIGN\\s+KEY|INDEX|KEY|CONSTRAINT|CHECK)\\b.*");
    }

    private String snakeToCamel(String str) {
        StringBuilder builder = new StringBuilder();
        for (String s : str.split("_")) {
            if (builder.length() == 0) {
                builder.append(s.toLowerCase());
            } else {
                builder.append(s.substring(0, 1).toUpperCase())
                        .append(s.substring(1).toLowerCase());
            }
        }
        return builder.toString();
    }

    public static class Table {
        private final String name;
        private final List<Column> columns;
        private final String comment;

        public Table(String name, List<Column> columns, String comment) {
            this.name = name;
            this.columns = columns;
            this.comment = comment;
        }

        public String getName() {
            return name;
        }

        public List<Column> getColumns() {
            return columns;
        }

        public String getComment() {
            return comment;
        }
    }

    public static class Column {
        private final String originalName;
        private final String name;
        private final String type;
        private final boolean notNull;
        private final String comment;

        public Column(String originalName, String name, String type, boolean notNull, String comment) {
            this.originalName = originalName;
            this.name = name;
            this.type = type;
            this.notNull = notNull;
            this.comment = comment;
        }

        public String getOriginalName() {
            return originalName;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public boolean isNotNull() {
            return notNull;
        }

        public String getComment() {
            return comment;
        }
    }
}