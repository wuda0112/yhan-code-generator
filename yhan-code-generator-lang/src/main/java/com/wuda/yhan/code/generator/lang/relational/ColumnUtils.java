package com.wuda.yhan.code.generator.lang.relational;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * column工具类.
 *
 * @author wuda
 */
public class ColumnUtils {

    /**
     * get column name list.
     *
     * @param columns list of column
     * @return list of column name
     */
    public static List<String> columnNames(List<Column> columns) {
        return columns.stream().map(Column::name).collect(Collectors.toList());
    }

    /**
     * 获取索引中的所有列.
     *
     * @param table 数据库中的表
     * @param index 代表数据库表的一个索引
     * @return 该索引中的所有列
     */
    public static List<Column> indexColumns(Table table, Index index) {
        String[] columnNames = index.getColumns();
        List<Column> columns = new ArrayList<>(columnNames.length);
        for (String columnName : columnNames) {
            Column column = table.columnWithName(columnName);
            columns.add(column);
        }
        return columns;
    }
}
