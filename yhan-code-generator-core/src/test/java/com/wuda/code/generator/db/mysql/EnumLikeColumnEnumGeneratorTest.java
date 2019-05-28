package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.JavaFile;
import com.wuda.yhan.code.generator.lang.relational.Table;
import org.junit.Test;

import java.util.List;

public class EnumLikeColumnEnumGeneratorTest extends GeneratorTestBase {

    @Test
    public void testGenJavaFile() {
        EnumLikeColumnEnumGenerator generator = new EnumLikeColumnEnumGenerator();
        TableTest tableTest = new TableTest();
        List<Table> tables = tableTest.getTable();
        for (Table table : tables) {
            List<JavaFile> javaFiles = generator.genJavaFile(table, packageName);
            for (JavaFile javaFile : javaFiles) {
                printAndWrite(javaFile);
            }
        }

    }
}
