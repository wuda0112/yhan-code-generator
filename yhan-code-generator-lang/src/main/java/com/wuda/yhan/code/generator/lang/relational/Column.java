package com.wuda.yhan.code.generator.lang.relational;

import io.debezium.relational.ColumnEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 一个hub,指向{@link io.debezium.relational.Column}.
 *
 * @author wuda
 */
public class Column implements io.debezium.relational.Column {

    /**
     * 实际的column.
     */
    private io.debezium.relational.Column actualColumn;

    /**
     * set actual column.
     *
     * @param actualColumn 实际的column
     */
    public void setActualColumn(io.debezium.relational.Column actualColumn) {
        this.actualColumn = actualColumn;
    }

    @Override
    public String name() {
        return actualColumn.name();
    }

    @Override
    public int position() {
        return actualColumn.position();
    }

    @Override
    public int jdbcType() {
        return actualColumn.jdbcType();
    }

    @Override
    public int nativeType() {
        return actualColumn.nativeType();
    }

    @Override
    public String typeName() {
        return actualColumn.typeName();
    }

    @Override
    public String typeExpression() {
        return actualColumn.typeExpression();
    }

    @Override
    public String charsetName() {
        return actualColumn.charsetName();
    }

    @Override
    public int length() {
        return actualColumn.length();
    }

    @Override
    public Optional<Integer> scale() {
        return actualColumn.scale();
    }

    @Override
    public boolean isOptional() {
        return actualColumn.isOptional();
    }

    @Override
    public boolean isAutoIncremented() {
        return actualColumn.isAutoIncremented();
    }

    @Override
    public boolean isGenerated() {
        return actualColumn.isGenerated();
    }

    @Override
    public Object defaultValue() {
        return actualColumn.defaultValue();
    }

    @Override
    public boolean hasDefaultValue() {
        return actualColumn.hasDefaultValue();
    }

    @Override
    public ColumnEditor edit() {
        throw new UnsupportedOperationException();
    }

    /**
     * 包装{@link io.debezium.relational.Column}.
     *
     * @param actualColumn 实际使用的column
     * @return column
     */
    public static Column wrap(io.debezium.relational.Column actualColumn) {
        Column columnWrapper = new Column();
        columnWrapper.setActualColumn(actualColumn);
        return columnWrapper;
    }

    /**
     * 包装{@link io.debezium.relational.Column}.
     *
     * @param actualColumns 实际使用的column
     * @return list of column
     */
    public static List<Column> wrap(List<io.debezium.relational.Column> actualColumns) {
        if (actualColumns == null || actualColumns.isEmpty()) {
            return null;
        }
        List<Column> list = new ArrayList<>(actualColumns.size());
        for (io.debezium.relational.Column actualColumn : actualColumns) {
            list.add(wrap(actualColumn));
        }
        return list;
    }
}
