package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.util.JavaNamingUtils;
import com.wuda.yhan.code.generator.lang.util.StringUtils;
import com.wuda.yhan.code.generator.lang.relational.Table;

public class SqlTableGeneratorUtil {

    /**
     * 类名称后缀.
     */
    private static String class_name_suffix = "SqlTable";

    /**
     * 根据表名生成类名.
     *
     * @param tableName 表名称
     * @return 类名
     */
    static String toClassName(String tableName) {
        String className = JavaNamingUtils.toCamelCase(tableName, Constant.word_separator);
        className = StringUtils.firstCharToUpperCase(className);
        className = StringUtils.addSuffix(className, class_name_suffix);
        return className;
    }

    /**
     * {@link TypeName}.
     *
     * @param table                  table
     * @param userSpecifyPackageName 包名
     * @return TypeName 实例
     */
    static TypeName getTypeName(Table table, String userSpecifyPackageName) {
        String className = toClassName(table.id().table());
        String packageName = PackageNameUtil.getSqlTablePackageName(userSpecifyPackageName, table.id().schema());
        return ClassName.get(packageName, className);
    }

}
