package com.wuda.code.generator.db.mysql;

import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.util.StringUtils;
import com.wuda.yhan.code.generator.lang.relational.Table;

/**
 * 命名工具类.
 *
 * @author wuda
 */
class PackageNameUtil {

    /**
     * 生成entity的包名.
     *
     * @param userSpecifyPackageName 用户指定的包名称
     * @param schema                 mysql schema
     * @return 两个相结合以后生成的包名
     */
    static String getEntityPackageName(String userSpecifyPackageName, String schema) {
        String packageName;
        if (schema != null && !schema.isEmpty()) {
            packageName = userSpecifyPackageName + ".entity." + dotSeparatedString(schema);
        } else {
            packageName = userSpecifyPackageName + ".entity";
        }
        return packageName;
    }

    /**
     * 生成mapper的包名.
     *
     * @param userSpecifyPackageName 用户指定的包名称
     * @param schema                 mysql schema
     * @return 两个相结合以后生成的包名
     */
    static String getMapperPackageName(String userSpecifyPackageName, String schema) {
        String packageName;
        if (schema != null && !schema.isEmpty()) {
            packageName = userSpecifyPackageName + ".mapper." + dotSeparatedString(schema);
        } else {
            packageName = userSpecifyPackageName + ".mapper";
        }
        return packageName;
    }

    /**
     * 生成mapper的包名.
     *
     * @param userSpecifyPackageName 用户指定的包名称
     * @param schema                 mysql schema
     * @return 两个相结合以后生成的包名
     */
    static String getEnumPackageName(String userSpecifyPackageName, String schema) {
        String packageName;
        if (schema != null && !schema.isEmpty()) {
            packageName = userSpecifyPackageName + ".enums." + dotSeparatedString(schema);
        } else {
            packageName = userSpecifyPackageName + ".enums";
        }
        return packageName;
    }

    private static String dotSeparatedString(String schema) {
        char dot = '.';
        String str = schema.toLowerCase().replace(Constant.underscore, dot);
        int lastCharIndex = StringUtils.lastCharIndex(str);
        if (str.charAt(lastCharIndex) == dot) {
            str = StringUtils.removeLastChar(str);
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

    /**
     * 生成sql table的包名.
     *
     * @param userSpecifyPackageName 用户指定的包名称
     * @param schema                 mysql schema
     * @return 两个相结合以后生成的包名
     */
    static String getSqlTablePackageName(String userSpecifyPackageName, String schema) {
        return getEntityPackageName(userSpecifyPackageName, schema);
    }

}