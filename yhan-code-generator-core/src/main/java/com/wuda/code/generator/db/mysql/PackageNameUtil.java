package com.wuda.code.generator.db.mysql;

import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.relational.Table;

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
            packageName = userSpecifyPackageName + ".entity." + schema.replace(Constant.word_separator + "", "");
        } else {
            packageName = userSpecifyPackageName + ".entity";
        }
        return packageName;
    }

    /**
     * schema.table
     *
     * @param table table
     * @return schema.table
     */
    static String getSchemaDotTable(Table table) {
        return table.id().schema() + "." + table.id().table();
    }

}
