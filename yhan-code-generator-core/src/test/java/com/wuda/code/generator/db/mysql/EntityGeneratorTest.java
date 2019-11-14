package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.JavaFile;
import com.wuda.yhan.code.generator.lang.relational.Table;
import org.junit.Test;

import java.util.List;

public class EntityGeneratorTest extends GeneratorTestBase {

    @Test
    public void testGenJavaFile() {
        EntityGenerator entityGenerator = new EntityGenerator();
        TableTest tableTest = new TableTest();
        List<Table> tables = tableTest.getTable();
        for (Table table : tables) {
            JavaFile javaFile = entityGenerator.genJavaFile(table, packageName, false);
            printAndWrite(javaFile);
        }

    }
}
