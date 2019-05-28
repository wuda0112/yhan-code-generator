package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.nio.file.Paths;

public class GeneratorTestBase {

    /**
     * 指定包名.
     */
    String packageName = "io.basex.dao.mysql";

    /**
     * 代码存放位置.
     */
    String path="e:/code/";

    public void printAndWrite(JavaFile javaFile) {
        try {
            javaFile.writeTo(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(javaFile.toString());
    }
}
