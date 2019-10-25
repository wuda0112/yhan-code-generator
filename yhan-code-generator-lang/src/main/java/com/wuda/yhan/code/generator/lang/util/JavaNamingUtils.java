package com.wuda.yhan.code.generator.lang.util;

/**
 * java命名工具类.比如getter/setter命名风格,驼峰命名风格.
 *
 * @author wuda
 */
public class JavaNamingUtils {

    /**
     * 字符串"get".
     */
    public static final String prefix_get = "get";
    /**
     * 字符串"set".
     */
    public static final String prefix_set = "set";

    /**
     * 转成驼峰命名.输入: product_name,输出: productName
     *
     * @param name
     *         名称
     * @param wordSeparator
     *         名称中单词之间的分隔符
     * @return 驼峰命名
     */
    public static String toCamelCase(String name, char wordSeparator) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        char[] charArray = name.toCharArray();
        char[] copy = new char[charArray.length];
        int index = 0;
        char ch;
        for (int i = 0; i < charArray.length; i++) {
            ch = charArray[i];
            if (ch == wordSeparator) {
                if (i == 0 || i == charArray.length - 1) { // first or last character
                    throw new RuntimeException("以分隔符开头或者结尾!name=" + name + ";separator=" + wordSeparator);
                }
                i++; // 步进到下一个位置
                ch = charArray[i];
                if (ch == wordSeparator) {
                    throw new RuntimeException("有连续的分隔符!name=" + name + ";separator=" + wordSeparator);
                }
                ch = Character.toUpperCase(ch);
            } else {
                ch = Character.toLowerCase(ch);
            }
            copy[index++] = ch;
        }
        return new String(copy, 0, index);
    }

    /**
     * 为属性生成getter方法名.
     *
     * @param fieldName
     *         属性名
     * @return getter 方法名
     */
    public static String genGetterMethodName(String fieldName) {
        String methodName = StringUtils.firstCharToUpperCase(fieldName);
        return StringUtils.addPrefix(methodName, prefix_get);
    }

    /**
     * 为属性生成setter方法名.
     *
     * @param fieldName
     *         属性名
     * @return setter 方法名
     */
    public static String genSetterMethodName(String fieldName) {
        String methodName = StringUtils.firstCharToUpperCase(fieldName);
        return StringUtils.addPrefix(methodName, prefix_set);
    }
}
