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
        Pattern pattern = Pattern.compile(
                "`?(\\w+)`?\\s+([a-z]+\\([\\d,\\s]+\\)|[a-z]+)\\s*(NOT NULL|NULL)?\\s*(DEFAULT\\s+[^,]+)?\\s*(?:COMMENT\\s+'([^']*)')?",
                Pattern.CASE_INSENSITIVE);

        String[] columnDefs = columnsDDL.split(",(?![^(]*\\))");
        for (String def : columnDefs) {
            def = def.trim();
            if (def.startsWith("PRIMARY KEY") || def.startsWith("INDEX")) continue;

            Matcher matcher = pattern.matcher(def);
            if (matcher.find()) {
                columns.add(new Column(
                        matcher.group(1), // originalName
                        snakeToCamel(matcher.group(1)), // camelCaseName
                        matcher.group(2).toUpperCase(), // type
                        !"NULL".equalsIgnoreCase(matcher.group(3)), // notNull
                        matcher.group(5) != null ? matcher.group(5) : "" // comment
                ));
            }
        }
        return columns;
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