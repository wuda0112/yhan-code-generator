package com.wuda.code.generator.db.mysql;

import com.wuda.yhan.code.generator.parser.mysql.MySqlCreateTableStatementParser;
import com.wuda.yhan.code.generator.lang.relational.Table;

import java.util.List;

public class TableTest {

    public List<Table> getTable() {

        String ddl = "CREATE TABLE cmp.`message_item` (\n" +
                "\t`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',\n" +
                "\t`message_category_id` INT(10) UNSIGNED NOT NULL COMMENT '所属消息分类',\n" +
                "\t`item_code` VARCHAR(45) NOT NULL COMMENT '唯一code，用于查找',\n" +
                "\t`item_name` VARCHAR(45) NOT NULL COMMENT '命名',\n" +
                "\t`creation_time` TIMESTAMP NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',\n" +
                "\tPRIMARY KEY (`id`),\n" +
                "\tUNIQUE INDEX `idx_item_code` (`item_code`),\n" +
                "\tINDEX `idx_message_category_id` (`message_category_id`)\n" +
                ")\n" +
                "COMMENT='具体的一种消息'\n" +
                "COLLATE='utf8_general_ci'\n" +
                "ENGINE=InnoDB\n" +
                "AUTO_INCREMENT=120\n" +
                ";\n";
        MySqlCreateTableStatementParser parser = new MySqlCreateTableStatementParser();
        return parser.parse(ddl);
    }

}
