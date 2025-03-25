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
     * @param ddl DDL 语句
     * @return Table 对象，包含表名和所有解析到的列；解析失败返回 null
     */
    public Table parseDDL(String ddl) {
        // 修改正则表达式，确保能捕获完整的表定义，包括字段类型、约束等
        Pattern tablePattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+`?(\\w+)`?\\s*\\((.*?)\\)\\s*;",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(ddl);

        if (tableMatcher.find()) {
            String tableName = tableMatcher.group(1).trim();
            String columnsDDL = tableMatcher.group(2).trim();
            List<Column> columns = parseColumns(columnsDDL);  // 解析所有列
            return new Table(tableName, columns);
        }
        return null;
    }

    private List<Column> parseColumns(String columnsDDL) {
        List<Column> columns = new ArrayList<>();

        // 修正正则表达式，确保可以匹配每一列定义
        // 这里的正则表达式将匹配字段名、类型、以及可能的约束（如NOT NULL、DEFAULT等）
        // 其中允许字段类型包含括号（例如：varchar(255)，int(11)）
        Pattern columnPattern = Pattern.compile(
                "`?(\\w+)`?\\s+([\\w(),]+)(\\s+(.*?))?\\s*(?=,|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher columnMatcher = columnPattern.matcher(columnsDDL);

        // 循环遍历每一列
        while (columnMatcher.find()) {
            String columnName = columnMatcher.group(1).trim();
            String columnType = columnMatcher.group(2).trim();
            String columnConstraints = columnMatcher.group(4);  // 可能的约束，如NOT NULL、DEFAULT等
            columns.add(new Column(columnName, columnType, columnConstraints));
        }
        return columns;
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
        private final String constraints;

        public Column(String name, String type, String constraints) {
            this.name = name;
            this.type = type;
            this.constraints = constraints;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getConstraints() {
            return constraints;
        }
    }
}