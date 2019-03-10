package com.wuda.code.generator.db.mysql;

import com.mysql.cj.MysqlType;
import org.junit.Test;

public class MysqlTypeTest {

    @Test
    public void test() {
        String fullMysqlTypeName = "int";
        MysqlType mysqlType = MysqlType.getByName(fullMysqlTypeName);
        System.out.println(mysqlType.getClassName());
    }
}
