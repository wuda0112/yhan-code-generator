package com.wuda.code.generator.db.mysql;

import com.wuda.code.generator.PairEnumGenerator;
import org.junit.Test;

import java.util.List;

public class EnumLikeColumnCommentParserTest {

    @Test
    public void test() {
        String comment = "enum.   ONE(   1-元素一) ||     TWO(2-元素二   )";
        EnumLikeColumnCommentParser parser = new EnumLikeColumnCommentParser();
        boolean accept = parser.accept(comment);
        if (accept) {
            List<PairEnumGenerator.EnumElement> list = parser.parse(Integer.class, comment);
            for (PairEnumGenerator.EnumElement element : list) {
                System.out.println("name: " + element.getName() + ",key= " + element.getKey() + ",value= " + element.getValue());
            }
        }
    }

}
