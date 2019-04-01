package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.nio.file.Paths;

public class GeneratorTestBase {

    String packageName = "com.wuda";

    public void printAndWrite(JavaFile javaFile) {
        try {
            javaFile.writeTo(Paths.get("e:/code/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(javaFile.toString());
    }
}
