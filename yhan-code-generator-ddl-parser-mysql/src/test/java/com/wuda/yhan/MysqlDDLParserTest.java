package com.wuda.yhan;

import com.wuda.yhan.code.generator.lang.relational.Table;
import org.junit.Test;

import java.util.List;

public class MysqlDDLParserTest extends TestBase {

    @Test
    public void testParse() {
        MySqlCreateTableStatementParser parser = new MySqlCreateTableStatementParser();
        List<Table> tables = parser.parse(ddl);
        for (Table table : tables) {
            System.out.println();
            System.out.println();
            System.out.println(table);
        }
    }

}
