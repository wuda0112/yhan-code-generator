package com.wuda.code.generator.db.mysql;

import com.wuda.yhan.MySqlCreateTableStatementParser;
import com.wuda.yhan.code.generator.lang.relational.Table;

import java.util.List;

public class TableTest {

    public List<Table> getTable() {

        String ddl = "CREATE TABLE `mysql_tester`.`individual_user` ("
                + "`id` BIGINT(20) UNSIGNED NOT NULL,"
                + "`username` VARCHAR(45) NOT NULL COMMENT '用户名',"
                + "`password` VARCHAR(45) NOT NULL COMMENT '密码',"
                + "`mobile_phone` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '手机',"
                + "`email` VARCHAR(45) NOT NULL DEFAULT '' COMMENT '邮箱',"
                + "`status` TINYINT(3) UNSIGNED NOT NULL DEFAULT '1' COMMENT '状态. \\\\n1 : 正常',"
                + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "`create_user` BIGINT(20) UNSIGNED NOT NULL,"
                + "`last_modify_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "`last_modify_user` BIGINT(20) UNSIGNED NOT NULL,"
                + "`is_deleted` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0',"
                + "PRIMARY KEY (`id`),"
                + "UNIQUE INDEX `idx_mobile_phone` (`mobile_phone`),"
                + "UNIQUE INDEX `idx_email` (`email`),"
                + "UNIQUE INDEX `idx_username` (`username`)"
                + ")"
                + "COMMENT='个人用户－基本信息'"
                + "COLLATE='utf8_general_ci'"
                + "ENGINE=InnoDB;" +

                "CREATE TABLE mysql_tester.`shop` ("
                + "`id` BIGINT(20) UNSIGNED NOT NULL,"
                + "`user_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '所属用户ID.',"
                + "`shop_name` VARCHAR(45) NOT NULL COMMENT '店铺名称',"
                + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "`create_user` BIGINT(20) UNSIGNED NOT NULL,"
                + "`last_modify_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "`last_modify_user` BIGINT(20) UNSIGNED NOT NULL,"
                + "`is_deleted` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0',"
                + "PRIMARY KEY (`id`),"
                + "INDEX `fk_user_id_idx` (`user_id`,`shop_name`)"
                + ")"
                + "COMMENT='店铺信息'"
                + "COLLATE='utf8_general_ci'"
                + "ENGINE=InnoDB;";
        MySqlCreateTableStatementParser parser = new MySqlCreateTableStatementParser();
        return parser.parse(ddl);
    }

}
