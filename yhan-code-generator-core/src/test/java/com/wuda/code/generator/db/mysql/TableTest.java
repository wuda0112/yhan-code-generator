package com.wuda.code.generator.db.mysql;

import java.util.ArrayList;
import java.util.List;

public class TableTest {

    public List<Table> getTable() {
        List<Table> tables = new ArrayList<>();
        tables.add(individualUser());
        tables.add(item());
        tables.add(item_category());
        tables.add(shop());
        tables.add(sku());
        tables.add(sku_warehouse_relationship());
        tables.add(warehouse());
        return tables;
    }

    private List<Table.ColumnMetaInfo> basicColumns() {
        Table.ColumnMetaInfo id = new Table.ColumnMetaInfo("id", "BIGINT(20) UNSIGNED", 1, "BIGINT", 20);

        Table.ColumnMetaInfo create_time = new Table.ColumnMetaInfo("create_time", "DATETIME", 100, "DATETIME", 0);
        Table.ColumnMetaInfo create_user = new Table.ColumnMetaInfo("create_user", "BIGINT(20) UNSIGNED", 101, "BIGINT", 20);
        Table.ColumnMetaInfo last_modify_time = new Table.ColumnMetaInfo("last_modify_time", "DATETIME", 102, "DATETIME", 0);
        Table.ColumnMetaInfo last_modify_user = new Table.ColumnMetaInfo("last_modify_user", "BIGINT(20) UNSIGNED", 103, "BIGINT", 20);
        Table.ColumnMetaInfo is_deleted = new Table.ColumnMetaInfo("is_deleted", "BIGINT(20) UNSIGNED", 104, "BIGINT", 20);
        List<Table.ColumnMetaInfo> basicColumns = new ArrayList<>(6);

        basicColumns.add(id);
        basicColumns.add(create_time);
        basicColumns.add(create_user);
        basicColumns.add(last_modify_time);
        basicColumns.add(last_modify_user);
        basicColumns.add(is_deleted);
        return basicColumns;
    }

    private void addCloumn(Table table, List<Table.ColumnMetaInfo> columns) {
        for (Table.ColumnMetaInfo columnMetaInfo : columns) {
            table.addColumn(columnMetaInfo);
        }
    }

    private Table individualUser() {
        Table table = new Table();
        table.setTableSchema("mysql_tester");
        table.setTableName("individual_user");

        List<Table.ColumnMetaInfo> basicColumns = basicColumns();
        addCloumn(table, basicColumns);
        table.addPrimaryKeyColumn(basicColumns.get(0));

        Table.ColumnMetaInfo username = new Table.ColumnMetaInfo("username", "VARCHAR(45)", 2, "VARCHAR", 45);
        Table.ColumnMetaInfo password = new Table.ColumnMetaInfo("password", "VARCHAR(45)", 3, "VARCHAR", 45);
        Table.ColumnMetaInfo mobile_phone = new Table.ColumnMetaInfo("mobile_phone", "INT(10) UNSIGNED", 4, "INT", 10);
        Table.ColumnMetaInfo email = new Table.ColumnMetaInfo("email", "VARCHAR(45)", 5, "VARCHAR", 45);
        Table.ColumnMetaInfo status = new Table.ColumnMetaInfo("status", "TINYINT(3) UNSIGNED", 6, "TINYINT", 3);

        table.addColumn(username);
        table.addColumn(password);
        table.addColumn(mobile_phone);
        table.addColumn(email);
        table.addColumn(status);

        return table;
    }

    private Table item() {
        Table table = new Table();
        table.setTableSchema("mysql_tester");
        table.setTableName("item");

        List<Table.ColumnMetaInfo> basicColumns = basicColumns();
        addCloumn(table, basicColumns);
        table.addPrimaryKeyColumn(basicColumns.get(0));

        Table.ColumnMetaInfo shop_id = new Table.ColumnMetaInfo("shop_id", "BIGINT(20) UNSIGNED", 2, "BIGINT", 20);
        Table.ColumnMetaInfo item_type = new Table.ColumnMetaInfo("item_type", "TINYINT(3) UNSIGNED", 3, "TINYINT", 3);
        Table.ColumnMetaInfo item_name = new Table.ColumnMetaInfo("item_name", "VARCHAR(45)", 4, "VARCHAR", 45);
        Table.ColumnMetaInfo category_one_id = new Table.ColumnMetaInfo("category_one_id", "BIGINT(20) UNSIGNED", 5, "BIGINT", 20);
        Table.ColumnMetaInfo category_two_id = new Table.ColumnMetaInfo("category_two_id", "BIGINT(20) UNSIGNED", 6, "BIGINT", 20);
        Table.ColumnMetaInfo category_three_id = new Table.ColumnMetaInfo("category_three_id", "BIGINT(20) UNSIGNED", 7, "BIGINT", 20);

        table.addColumn(shop_id);
        table.addColumn(item_type);
        table.addColumn(item_name);
        table.addColumn(category_one_id);
        table.addColumn(category_two_id);
        table.addColumn(category_three_id);

        return table;
    }

    private Table item_category() {
        Table table = new Table();
        table.setTableSchema("mysql_tester");
        table.setTableName("item_category");

        List<Table.ColumnMetaInfo> basicColumns = basicColumns();
        addCloumn(table, basicColumns);
        table.addPrimaryKeyColumn(basicColumns.get(0));

        Table.ColumnMetaInfo category_name = new Table.ColumnMetaInfo("category_name", "VARCHAR(45)", 2, "VARCHAR", 45);
        Table.ColumnMetaInfo parent_id = new Table.ColumnMetaInfo("parent_id", "BIGINT(20) UNSIGNED", 3, "BIGINT", 20);

        table.addColumn(category_name);
        table.addColumn(parent_id);

        return table;
    }

    private Table shop() {
        Table table = new Table();
        table.setTableSchema("mysql_tester");
        table.setTableName("shop");

        List<Table.ColumnMetaInfo> basicColumns = basicColumns();
        addCloumn(table, basicColumns);
        table.addPrimaryKeyColumn(basicColumns.get(0));

        Table.ColumnMetaInfo user_id = new Table.ColumnMetaInfo("user_id", "BIGINT(20) UNSIGNED", 2, "BIGINT", 20);
        Table.ColumnMetaInfo shop_name = new Table.ColumnMetaInfo("shop_name", "VARCHAR(45)", 3, "VARCHAR", 45);

        table.addColumn(user_id);
        table.addColumn(shop_name);

        return table;
    }

    private Table sku() {
        Table table = new Table();
        table.setTableSchema("mysql_tester");
        table.setTableName("sku");

        List<Table.ColumnMetaInfo> basicColumns = basicColumns();
        addCloumn(table, basicColumns);
        table.addPrimaryKeyColumn(basicColumns.get(0));

        Table.ColumnMetaInfo item_id = new Table.ColumnMetaInfo("item_id", "BIGINT(20) UNSIGNED", 2, "BIGINT", 20);
        Table.ColumnMetaInfo shop_id = new Table.ColumnMetaInfo("shop_id", "BIGINT(20) UNSIGNED", 3, "BIGINT", 20);

        table.addColumn(item_id);
        table.addColumn(shop_id);

        return table;
    }

    private Table sku_warehouse_relationship() {
        Table table = new Table();
        table.setTableSchema("mysql_tester");
        table.setTableName("sku_warehouse_relationship");

        List<Table.ColumnMetaInfo> basicColumns = basicColumns();
        addCloumn(table, basicColumns);
        table.addPrimaryKeyColumn(basicColumns.get(0));

        Table.ColumnMetaInfo sku_id = new Table.ColumnMetaInfo("sku_id", "BIGINT(20) UNSIGNED", 2, "BIGINT", 20);
        Table.ColumnMetaInfo warehouse_id = new Table.ColumnMetaInfo("warehouse_id", "BIGINT(20) UNSIGNED", 3, "BIGINT", 20);
        Table.ColumnMetaInfo total_quantity = new Table.ColumnMetaInfo("total_quantity", "INT(10) UNSIGNED", 4, "INT", 10);

        table.addColumn(sku_id);
        table.addColumn(warehouse_id);
        table.addColumn(total_quantity);

        return table;
    }

    private Table warehouse() {
        Table table = new Table();
        table.setTableSchema("mysql_tester");
        table.setTableName("warehouse");

        List<Table.ColumnMetaInfo> basicColumns = basicColumns();
        addCloumn(table, basicColumns);
        table.addPrimaryKeyColumn(basicColumns.get(0));

        Table.ColumnMetaInfo user_id = new Table.ColumnMetaInfo("user_id", "BIGINT(20) UNSIGNED", 2, "BIGINT", 20);
        Table.ColumnMetaInfo shop_id = new Table.ColumnMetaInfo("shop_id", "BIGINT(20) UNSIGNED", 3, "BIGINT", 20);
        Table.ColumnMetaInfo warehouse_type = new Table.ColumnMetaInfo("warehouse_type", "TINYINT(3) UNSIGNED", 4, "TINYINT", 3);
        Table.ColumnMetaInfo warehouse_name = new Table.ColumnMetaInfo("warehouse_name", "VARCHAR(45)", 5, "VARCHAR", 45);

        table.addColumn(user_id);
        table.addColumn(shop_id);
        table.addColumn(warehouse_type);
        table.addColumn(warehouse_name);

        return table;
    }
}
