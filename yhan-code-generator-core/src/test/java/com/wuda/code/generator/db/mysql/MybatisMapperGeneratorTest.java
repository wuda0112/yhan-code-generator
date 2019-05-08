package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.JavaFile;
import com.wuda.yhan.code.generator.lang.relational.Table;
import org.junit.Test;

import java.util.List;

public class MybatisMapperGeneratorTest extends GeneratorTestBase {

    @Test
    public void testGenJavaFile() {
        TableTest tableTest = new TableTest();
        MyBatisMapperGenerator myBatisMapperGenerator = new MyBatisMapperGenerator();
        List<Table> tables = tableTest.getTable();
        for (Table table : tables) {
            JavaFile javaFile = myBatisMapperGenerator.genJavaFile(table, packageName);
            printAndWrite(javaFile);
        }
    }
}
