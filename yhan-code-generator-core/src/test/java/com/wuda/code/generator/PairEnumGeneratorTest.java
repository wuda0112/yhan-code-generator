package com.wuda.code.generator;

import com.squareup.javapoet.JavaFile;
import com.wuda.code.generator.db.mysql.GeneratorTestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PairEnumGeneratorTest extends GeneratorTestBase {

    @Test
    public void test() {
        PairEnumGenerator<Integer, String> generator = new PairEnumGenerator<>();
        PairEnumGenerator.EnumElement one = generator.new EnumElement("ONE", 1, "A");
        PairEnumGenerator.EnumElement two = generator.new EnumElement("TWO", 2, "B");
        List<PairEnumGenerator.EnumElement> elements = Arrays.asList(one, two);
//        List<PairEnumGenerator.EnumElement> elements = Collections.emptyList();
        JavaFile javaFile = generator.genJavaFile("com.wuda.code.generator", "TestEnum", elements);
        printAndWrite(javaFile);
    }
}
