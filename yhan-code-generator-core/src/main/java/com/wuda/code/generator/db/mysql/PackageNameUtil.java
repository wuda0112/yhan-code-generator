package com.wuda.code.generator.db.mysql;

import com.wuda.yhan.code.generator.lang.Constant;

/**
 * 命名工具类.
 *
 * @author wuda
 */
class PackageNameUtil {

    /**
     * 生成包名.
     *
     * @param userSpecifyPackageName
     *         用户指定的包名称
     * @param schema
     *         mysql schema
     * @return 两个相结合以后生成的包名
     */
    static String getPackageName(String userSpecifyPackageName, String schema) {
//        return userSpecifyPackageName + "." + schema.replace(Constant.word_separator + "", "");
        return userSpecifyPackageName + ".entity";
    }

    /**
     * schema.table
     *
     * @param table
     *         table
     * @return schema.table
     */
    static String getSchemaDotTable(Table table) {
        return table.getTableSchema() + "." + table.getTableName();
    }

}
