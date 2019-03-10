package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.JavaFile;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class EntityGeneratorTest {

    @Test
    public void testGenJavaFile() {
        EntityGenerator entityGenerator = new EntityGenerator();
        TableTest tableTest = new TableTest();
        List<Table> tables=tableTest.getTable();
        for(Table table : tables){
            JavaFile javaFile = entityGenerator.genJavaFile(table, "com.wuda.tester.mysql");
            try {
                javaFile.writeTo(Paths.get("e:/code/"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(javaFile.toString());
        }

    }
}
