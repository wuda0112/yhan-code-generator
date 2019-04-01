package com.wuda.code.generator.db.mysql;

import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Table;
import io.debezium.connector.mysql.MySqlDdlParser;
import io.debezium.relational.TableId;
import io.debezium.relational.Tables;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TableTest {

    public List<Table> getTable() {
        Tables tables = new Tables();
        String ddl = "CREATE TABLE test.shop ("
                + " id BIGINT(20) NOT NULL AUTO_INCREMENT,"
                + " version BIGINT(20) NOT NULL,"
                + " name VARCHAR(255) NOT NULL,"
                + " owner VARCHAR(255) NOT NULL,"
                + " phone_number VARCHAR(255) NOT NULL,"
                + " primary key (id, name)"
                + " );";
        MySqlDdlParser parser = new MySqlDdlParser();
        parser.parse(ddl, tables);
        Set<TableId> tableIds = tables.tableIds();
        List<Table> tableList = new ArrayList<>(tableIds.size());
        for (TableId tableId : tableIds) {
            io.debezium.relational.Table actualTable = tables.forTable(tableId);
            Table table = new Table();
            table.setActualTable(actualTable);
            tableList.add(table);
        }
        return tableList;
    }

    @Test
    public void test() {
        List<Table> tables = getTable();
        for (Table table : tables) {
            List<Column> columns = table.nonPrimaryKeyColumns();
            printColumn(columns);
        }
    }

    private void printColumn(List<Column> columns) {
        columns.forEach(column -> {
            System.out.println(column.name());
        });
    }

}
