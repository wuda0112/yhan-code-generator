package com.wuda.code.generator.db.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Comparator;
import java.util.TreeSet;

import javax.persistence.Column;

/**
 * 描述一个数据库中的表.
 *
 * @author wuda
 */
@Getter
public class Table {

    /**
     * schema.
     */
    private String tableSchema;
    /**
     * 表名.
     */
    private String tableName;
    /**
     * 列.
     */
    private TreeSet<ColumnMetaInfo> columns = new TreeSet<>(columnOrderComparator());
    /**
     * 主键.
     */
    private TreeSet<ColumnMetaInfo> primaryKey = new TreeSet<>(columnOrderComparator());

    /**
     * 检查表的信息是否完整.
     */
    public void checkValid() {
        if (tableSchema == null || tableSchema.isEmpty()) {
            throw new IllegalStateException("schema为空!");
        }
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("表名为空!");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalStateException(tableSchema + "." + tableName + "表中没有column!");
        }
        if (primaryKey == null || primaryKey.isEmpty()) {
            throw new IllegalStateException(tableSchema + "." + tableName + "表中没有主键!");
        }
    }

    /**
     * 绝大部分情况下,表的主键都是一个字段,但是也不排除多个字段组成的主键.
     *
     * @return 主键的字段
     */
    public String[] getPrimaryKeyColumns() {
        String[] primaryKeyColumns = new String[this.primaryKey.size()];
        int index = 0;
        for (ColumnMetaInfo columnMetaInfo : this.primaryKey) {
            primaryKeyColumns[index++] = columnMetaInfo.columnName;
        }
        return primaryKeyColumns;
    }

    /**
     * 设置schema.
     *
     * @param tableSchema
     *         schema
     * @return this
     */
    public Table setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
        return this;
    }

    /**
     * 设置表名.
     *
     * @param tableName
     *         表名
     * @return this
     */
    public Table setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * 添加列信息.
     *
     * @param column
     *         列
     * @return this
     */
    public Table addColumn(ColumnMetaInfo column) {
        columns.add(column);
        return this;
    }

    /**
     * 添加主键字段.
     *
     * @param column
     *         column
     * @return this
     */
    public Table addPrimaryKeyColumn(ColumnMetaInfo column) {
        primaryKey.add(column);
        return this;
    }

    private Comparator<ColumnMetaInfo> columnOrderComparator() {
        return Comparator.comparingInt(ColumnMetaInfo::getOrdinalPosition);
    }

    /**
     * 列的信息.
     *
     * @author wuda
     */
    @Getter
    @AllArgsConstructor
    public static class ColumnMetaInfo {
        /**
         * 列名.
         */
        private String columnName;
        /**
         * column的定义.比如: VARCHAR(45) NOT NULL DEFAULT '代码生成'.
         * 参考{@link Column#columnDefinition()}
         */
        private String columnDefinition;
        /**
         * 排列顺序.
         */
        private int ordinalPosition;
        /**
         * 数据类型.
         */
        private String dataType;
        /**
         * 数据的长度.
         */
        private int length;
    }
}
