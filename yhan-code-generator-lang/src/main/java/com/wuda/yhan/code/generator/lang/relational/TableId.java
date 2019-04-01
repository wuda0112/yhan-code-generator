package com.wuda.yhan.code.generator.lang.relational;

/**
 * hub,指向{@link io.debezium.relational.TableId}.
 *
 * @author wuda
 */
public final class TableId implements Comparable<TableId> {

    /**
     * 实际的tableId.
     */
    private io.debezium.relational.TableId actualTableId;

    /**
     * 包装{@link io.debezium.relational.TableId}.
     *
     * @param actualTableId 实际使用的table id实例
     * @return table id instance
     */
    public static TableId wrap(io.debezium.relational.TableId actualTableId) {
        TableId tableId = new TableId();
        tableId.setActualTableId(actualTableId);
        return tableId;
    }

    /**
     * 设置{@link io.debezium.relational.TableId}.
     *
     * @param actualTableId 实际使用的tableId类
     */
    public void setActualTableId(io.debezium.relational.TableId actualTableId) {
        this.actualTableId = actualTableId;
    }

    /**
     * Parse the supplied string, extracting up to the first 3 parts into a TableID.
     *
     * @param str the string representation of the table identifier; may not be null
     * @return the table ID, or null if it could not be parsed
     */
    public static TableId parse(String str) {
        io.debezium.relational.TableId tableId = io.debezium.relational.TableId.parse(str);
        return wrap(tableId);
    }

    /**
     * Parse the supplied string, extracting up to the first 3 parts into a TableID.
     *
     * @param str                    the string representation of the table identifier; may not be null
     * @param useCatalogBeforeSchema {@code true} if the parsed string contains only 2 items and the first should be used as
     *                               the catalog and the second as the table name, or {@code false} if the first should be used as the schema and the
     *                               second as the table name
     * @return the table ID, or null if it could not be parsed
     */
    public static TableId parse(String str, boolean useCatalogBeforeSchema) {
        io.debezium.relational.TableId tableId = io.debezium.relational.TableId.parse(str, useCatalogBeforeSchema);
        return wrap(tableId);
    }

    /**
     * Get the name of the JDBC catalog.
     *
     * @return the catalog name, or null if the table does not belong to a catalog
     */
    public String catalog() {
        return actualTableId.catalog();
    }

    /**
     * Get the name of the JDBC schema.
     *
     * @return the JDBC schema name, or null if the table does not belong to a JDBC schema
     */
    public String schema() {
        String schema = actualTableId.schema();
        if (schema == null || schema.isEmpty()) {
            schema = actualTableId.catalog();
        }
        return schema;
    }

    /**
     * Get the name of the table.
     *
     * @return the table name; never null
     */
    public String table() {
        return actualTableId.table();
    }

    @Override
    public int compareTo(TableId that) {
        if (this == that) {
            return 0;
        }
        return this.actualTableId.compareTo(that.actualTableId);
    }

    public int compareToIgnoreCase(TableId that) {
        if (this == that) {
            return 0;
        }
        return this.actualTableId.compareToIgnoreCase(that.actualTableId);
    }

    @Override
    public int hashCode() {
        return actualTableId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TableId) {
            return this.compareTo((TableId) obj) == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return actualTableId.toString();
    }

    /**
     * Returns a dot-separated String representation of this identifier, quoting all
     * name parts with the {@code "} char.
     */
    public String toDoubleQuotedString() {
        return toQuotedString('"');
    }

    /**
     * Returns a dot-separated String representation of this identifier, quoting all
     * name parts with the given quoting char.
     */
    public String toQuotedString(char quotingChar) {
        return actualTableId.toQuotedString(quotingChar);
    }

    public TableId toLowercase() {
        io.debezium.relational.TableId tableId = actualTableId.toLowercase();
        return wrap(tableId);
    }
}
