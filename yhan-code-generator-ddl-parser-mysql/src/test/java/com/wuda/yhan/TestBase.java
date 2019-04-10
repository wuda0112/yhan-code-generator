package com.wuda.yhan;

public class TestBase {

    String ddl = "CREATE TABLE `test`.`individual_user` ("
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

            "CREATE TABLE `shop` ("
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
            + "ENGINE=InnoDB;" +

            "CREATE TABLE test.shop ("
            + " id BIGINT(20) NOT NULL AUTO_INCREMENT,"
            + " version BIGINT(20) NOT NULL,"
            + " name VARCHAR(255) NOT NULL,"
            + " owner VARCHAR(255) NOT NULL,"
            + " phone_number VARCHAR(255) NOT NULL,"
            + " primary key (id, name)"
            + " );";
}
