package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.JavaFile;
import com.wuda.yhan.code.generator.lang.relational.Table;
import org.junit.Test;

import java.util.List;

public class SqlTableGeneratorTest extends GeneratorTestBase {

    @Test
    public void testGenJavaFile() {
        SqlTableGenerator generator = new SqlTableGenerator();
        TableTest tableTest = new TableTest();
        List<Table> tables = tableTest.getTable();
        for (Table table : tables) {
            JavaFile javaFile = generator.genJavaFile(table, packageName);
            printAndWrite(javaFile);
        }

    }
}
