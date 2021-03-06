package com.wuda.yhan.code.generator.lang.relational;

import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 一个hub,指向{@link io.debezium.relational.Table},
 * 同时也做了索引信息的扩展,因为{@link io.debezium.relational.Table}的定义中
 * 不包含索引信息(除了主键以为的其他索引).
 *
 * @author wuda
 */
@ToString
public class Table {

    /**
     * 实际的table.
     */
    private io.debezium.relational.Table actualTable;

    private HashMap<String, Column> WRAPPED_COLUMN_CACHE = new HashMap<>();

    /**
     * set actual table
     *
     * @param actualTable 实际的table
     */
    public void setActualTable(io.debezium.relational.Table actualTable) {
        this.actualTable = actualTable;
    }

    /**
     * 表中的索引(除了主键).
     */
    private List<Index> indices;

    /**
     * 获取表中的索引.
     *
     * @return 索引, 不包含主键
     */
    public List<Index> getIndices() {
        return indices;
    }

    /**
     * 设置索引信息.
     *
     * @param indices 索引信息,不包含主键
     */
    public void setIndices(List<Index> indices) {
        this.indices = indices;
    }

    public TableId id() {
        io.debezium.relational.TableId tableId = actualTable.id();
        return TableId.wrap(tableId);
    }

    public List<String> primaryKeyColumnNames() {
        return actualTable.primaryKeyColumnNames();
    }

    public List<Column> primaryKeyColumns() {
        List<io.debezium.relational.Column> primaryKeyColumns = actualTable.primaryKeyColumns();
        return Column.wrap(primaryKeyColumns);
    }

    public List<Column> nonPrimaryKeyColumns() {
        List<io.debezium.relational.Column> nonPrimaryKeyColumns = actualTable.nonPrimaryKeyColumns();
        return Column.wrap(nonPrimaryKeyColumns);
    }

    public List<String> retrieveColumnNames() {
        return actualTable.retrieveColumnNames();
    }


    public List<Column> columns() {
        List<io.debezium.relational.Column> actualColumns = actualTable.columns();
        List<Column> wrappedColumns = new ArrayList<>(actualColumns.size());
        String columnName;
        for (io.debezium.relational.Column actualColumn : actualColumns) {
            columnName = actualColumn.name();
            wrappedColumns.add(columnWithName(columnName));
        }
        return wrappedColumns;
    }

    public Column columnWithName(String name) {
        if (!WRAPPED_COLUMN_CACHE.containsKey(name)) {
            io.debezium.relational.Column actualColumn = actualTable.columnWithName(name);
            WRAPPED_COLUMN_CACHE.put(name, Column.wrap(actualColumn));
        }
        return WRAPPED_COLUMN_CACHE.get(name);
    }

    public String defaultCharsetName() {
        return actualTable.defaultCharsetName();
    }

    public boolean isPrimaryKeyColumn(String columnName) {
        return actualTable.isPrimaryKeyColumn(columnName);
    }

    public boolean isAutoIncremented(String columnName) {
        return actualTable.isAutoIncremented(columnName);
    }

    public boolean isGenerated(String columnName) {
        return actualTable.isGenerated(columnName);
    }

    public boolean isOptional(String columnName) {
        return actualTable.isOptional(columnName);
    }
}
