package com.wuda.code.generator.db.mysql;

import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.relational.Table;
import com.wuda.yhan.util.commons.StringUtil;

/**
 * 命名工具类.
 *
 * @author wuda
 */
class PackageNameUtil {

    /**
     * 生成包名.
     *
     * @param userSpecifyPackageName 用户指定的包名称
     * @param schema                 mysql schema
     * @return 两个相结合以后生成的包名
     */
    static String getPackageName(String userSpecifyPackageName, String schema) {
        String packageName;
        if (schema != null && !schema.isEmpty()) {
            packageName = userSpecifyPackageName + ".entity." + dotSeparatedString(schema);
        } else {
            packageName = userSpecifyPackageName + ".entity";
        }
        return packageName;
    }

    private static String dotSeparatedString(String schema) {
        char dot = '.';
        String str = schema.toLowerCase().replace(Constant.word_separator, dot);
        int lastCharIndex = StringUtil.lastCharIndex(str);
        if (str.charAt(lastCharIndex) == dot) {
            str = StringUtil.removeLastChar(str);
        } else if (str.charAt(0) == dot) {
            str = str.substring(1);
        }
        return str;
    }

    /**
     * schema dot table
     *
     * @param table {@link Table}
     * @return schema.table
     */
    static String getSchemaDotTable(Table table) {
        return table.id().schema() + "." + table.id().table();
    }

}