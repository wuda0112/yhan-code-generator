package com.wuda.code.generator.db.mysql;

import com.wuda.yhan.MySqlCreateTableStatementParser;
import com.wuda.yhan.code.generator.lang.relational.Table;

import java.util.List;

public class TableTest {

    public List<Table> getTable() {

        String ddl = "CREATE TABLE `my_schema`.`user_basic` (\n" +
                "\t`id` INT(10) UNSIGNED NOT NULL,\n" +
                "\t`username` VARCHAR(50) NULL DEFAULT NULL,\n" +
                "\t`nickname` VARCHAR(50) NULL DEFAULT NULL,\n" +
                "\tPRIMARY KEY (`id`),\n" +
                "\tUNIQUE INDEX `idx_username` (`username`),\n" +
                "\tINDEX `idx_nickname` (`nickname`)\n" +
                ")";
        MySqlCreateTableStatementParser parser = new MySqlCreateTableStatementParser();
        return parser.parse(ddl);
    }

}
