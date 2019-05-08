package com.wuda.code.generator.db.mysql;

import com.wuda.yhan.MySqlCreateTableStatementParser;
import com.wuda.yhan.code.generator.lang.relational.Table;

import java.util.List;

public class TableTest {

    public List<Table> getTable() {

        String ddl = "CREATE TABLE `user_basic` (\n" +
                "\t`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
                "\t`username` VARCHAR(50) NULL DEFAULT NULL,\n" +
                "\t`nickname` VARCHAR(50) NULL DEFAULT NULL,\n" +
                "\t`phone` VARCHAR(45) NULL DEFAULT NULL,\n" +
                "\t`is_deleted` TINYINT(4) NOT NULL DEFAULT '0',\n" +
                "\tPRIMARY KEY (`id`),\n" +
                "\tUNIQUE INDEX `idx_username` (`username`),\n" +
                "\tUNIQUE INDEX `idx_phone_is_deleted` (`phone`, `is_deleted`),\n" +
                "\tINDEX `idx_nickname` (`nickname`)\n" +
                ")\n" +
                "COLLATE='utf8_general_ci'\n" +
                "ENGINE=InnoDB\n" +
                "AUTO_INCREMENT=21\n" +
                ";";
        MySqlCreateTableStatementParser parser = new MySqlCreateTableStatementParser();
        return parser.parse(ddl);
    }

}
