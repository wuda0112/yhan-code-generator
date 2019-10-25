package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.util.JavaNamingUtils;
import com.wuda.yhan.code.generator.lang.util.StringUtils;
import com.wuda.yhan.code.generator.lang.relational.Table;

/**
 * {@link SqlBuilderGenerator}生成代码时,命名工具类.
 *
 * @author wuda
 */
class SqlBuilderGeneratorUtil {

    /**
     * class name suffix.
     */
    private static String suffix = "SqlBuilder";

    /**
     * 根据表名生成类名.
     *
     * @param tableName 表名称
     * @return 类名
     */
    static String toClassName(String tableName) {
        String className = JavaNamingUtils.toCamelCase(tableName, Constant.word_separator);
        className = StringUtils.firstCharToUpperCase(className);
        className = StringUtils.addSuffix(className, suffix);
        return className;
    }

    /**
     * 获取每个表对应的sql builder类的{@link TypeName}.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return 每个表对应的sql builder类的{@link TypeName}
     */
    static TypeName getSqlBuilderTypeName(Table table, String userSpecifyPackageName) {
        String className = SqlBuilderGeneratorUtil.toClassName(table.id().table());
        String finalPackageName = PackageNameUtil.getMapperPackageName(userSpecifyPackageName, table.id().schema());
        return ClassName.get(finalPackageName, className);
    }

}
