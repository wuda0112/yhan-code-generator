package com.wuda.code.generator.db.mysql;

import com.wuda.code.generator.PairEnumGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库中column的定义不是enum或者set,但是表示类似意思,比如status列,通常我们定义为tinyint,
 * 然后在注释中说明1代表xx,2代表yy,像这样的列的定义是很多的,而且具有规律性,因此可以统一为这样的
 * 列生成对应的枚举,前提条件是列的注释满足一定的规范,只要是满足规范的注释,就可以方便解析.因此
 * 这个类的作用就是定义和解析注释.
 *
 * @author wuda
 */
public class EnumLikeColumnCommentParser {

    private static String prefix = "enum.";

    /**
     * 给定的注释是满足规范的,是可以被解析的.
     *
     * @param comment 列的注释
     * @return true-如果注释是可以被解析的
     */
    public boolean accept(String comment) {
        return comment != null && !comment.isEmpty() && comment.startsWith(prefix);
    }

    /**
     * 解析注释,得到生成枚举的元素.
     * 调用此方法之前,必须首先调用{@link #accept(String)},
     * 只有返回<code>true</code>时才能继续调用此方法.
     *
     * @param columnJavaType 列的数据类型,对应的Java type
     * @param columnComment  列的注释,必须满足一定规则
     * @return 生成枚举的元素
     */
    public List<PairEnumGenerator.EnumElement> parse(Class columnJavaType, String columnComment) {
        String commentCopy = columnComment.substring(prefix.length());
        String[] elements = commentCopy.split("\\|\\|");
        String name = null;
        String code = null;
        String desc = null;
        int left_parenthesis_position = -1;
        int right_parenthesis_position = -1;
        int hyphen_position = -1;

        List<PairEnumGenerator.EnumElement> list = new ArrayList<>();
        for (String element : elements) {
            char[] chars = element.toCharArray();
            for (int offset = 0; offset < chars.length; offset++) {
                char ch = chars[offset];
                if (ch == '(') {
                    left_parenthesis_position = offset;
                    name = element.substring(0, left_parenthesis_position).trim();
                } else if (ch == '-') {
                    hyphen_position = offset;
                    code = element.substring(left_parenthesis_position + 1, hyphen_position).trim();
                } else if (ch == ')') {
                    right_parenthesis_position = offset;
                    desc = element.substring(hyphen_position + 1, right_parenthesis_position).trim();
                }
            }
            list.add(new PairEnumGenerator.EnumElement<>(name, toTypedValue(columnJavaType, code), desc));
        }
        return list;
    }

    private Object toTypedValue(Class clazz, String value) {
        if (clazz.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (clazz.equals(Long.class)) {
            return Long.parseLong(value);
        }
        throw new UnsupportedOperationException("clazz=" + clazz + ",不支持的数据类型");
    }

}
