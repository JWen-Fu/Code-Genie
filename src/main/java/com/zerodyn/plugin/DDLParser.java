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
        // 修改正则：允许任意空格，忽略大小写，捕获表名和括号中的内容
        Pattern tablePattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+`(.*?)`\\s*\\((.*?)\\)\\s*.*?;",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(ddl);

        if (tableMatcher.find()) {
            String tableName = tableMatcher.group(1).trim();
            String columnsDDL = tableMatcher.group(2);
            List<Column> columns = parseColumns(columnsDDL);
            return new Table(tableName, columns);
        }
        return null;
    }

    /**
     * 解析列定义，忽略 PRIMARY KEY、UNIQUE KEY 等约束行
     * @param columnsDDL 括号内的所有内容
     * @return 列列表
     */
    private List<Column> parseColumns(String columnsDDL) {
        List<Column> columns = new ArrayList<>();
        // 使用正则匹配以 ` 开头的列定义，捕获列名和列类型及其他属性，直到遇到逗号或行尾
        Pattern columnPattern = Pattern.compile(
                "\\s*`(\\w+)`\\s+([^,]+)(,|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher columnMatcher = columnPattern.matcher(columnsDDL);

        while (columnMatcher.find()) {
            String colName = columnMatcher.group(1).trim();
            String colDefinition = columnMatcher.group(2).trim();
            // 排除以 PRIMARY 或 UNIQUE 开头的约束行（虽然一般不会以 ` 开头）
            if (colName.equalsIgnoreCase("primary") || colName.equalsIgnoreCase("unique")) {
                continue;
            }
            columns.add(new Column(colName, colDefinition));
        }
        return columns;
    }

    // 内部类：Table 表示解析得到的表信息
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

    // 内部类：Column 表示解析得到的列信息
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