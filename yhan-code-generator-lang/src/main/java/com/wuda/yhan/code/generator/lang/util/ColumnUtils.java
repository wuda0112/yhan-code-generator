package com.wuda.yhan.code.generator.lang.util;

import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Index;
import com.wuda.yhan.code.generator.lang.relational.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link Column}的工具类.
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

    /**
     * 获取<i>AUTO_INCREMENT</i>列.
     *
     * @param table table
     * @return AUTO_INCREMENT column or null if not exist
     */
    public static Column getAutoIncrementColumn(Table table) {
        List<Column> primaryKeys = table.primaryKeyColumns();
        Column autoIncrementColumn = null;
        for (Column column : primaryKeys) {
            if (column.isAutoIncremented()) {
                autoIncrementColumn = column;
                break;
            }
        }
        return autoIncrementColumn;
    }
}
