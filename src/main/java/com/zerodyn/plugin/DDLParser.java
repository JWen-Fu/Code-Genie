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
    // Method to parse a CREATE TABLE DDL statement
    public Table parseDDL(String ddl) {
        // Regular expression to match table creation
        Pattern tablePattern = Pattern.compile("CREATE TABLE `(.*?)` \\((.*?)\\);", Pattern.DOTALL);
        Matcher tableMatcher = tablePattern.matcher(ddl);

        if (tableMatcher.find()) {
            String tableName = tableMatcher.group(1);  // Table name
            String columnsDDL = tableMatcher.group(2); // Columns and constraints

            // Parse columns from DDL
            List<Column> columns = parseColumns(columnsDDL);

            return new Table(tableName, columns);
        }

        return null;
    }

    // Helper method to parse columns from the DDL
    private List<Column> parseColumns(String columnsDDL) {
        List<Column> columns = new ArrayList<>();
        Pattern columnPattern = Pattern.compile("`(.*?)`\\s+(.*?),");
        Matcher columnMatcher = columnPattern.matcher(columnsDDL);

        while (columnMatcher.find()) {
            String columnName = columnMatcher.group(1);
            String columnType = columnMatcher.group(2);
            columns.add(new Column(columnName, columnType));
        }

        // To handle the last column without a comma
        if (columnsDDL.trim().endsWith(")")) {
            Matcher lastColumnMatcher = columnPattern.matcher(columnsDDL.trim() + ",");
            if (lastColumnMatcher.find()) {
                String columnName = lastColumnMatcher.group(1);
                String columnType = lastColumnMatcher.group(2);
                columns.add(new Column(columnName, columnType));
            }
        }

        return columns;
    }

    // Inner classes to hold parsed table and column information
    public static class Table {
        private String name;
        private List<Column> columns;

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
        private String name;
        private String type;

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
