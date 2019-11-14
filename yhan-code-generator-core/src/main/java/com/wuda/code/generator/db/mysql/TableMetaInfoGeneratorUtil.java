package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.relational.Table;
import com.wuda.yhan.code.generator.lang.util.JavaNamingUtils;
import com.wuda.yhan.code.generator.lang.util.StringUtils;

/**
 * {@link TableMetaInfoGenerator}生成代码时,命名工具类.
 *
 * @author wuda
 */
class TableMetaInfoGeneratorUtil {

    /**
     * 类名称后缀.
     */
    private static String class_name_suffix = "MetaInfo";
    /**
     * sql 列的别名,"AS"语法.
     */
    private static String column_name_as_suffix = "_AS";
    /**
     * sql 列的别名,"AS"语法.
     */
    private static String column_name_concat_table_as_suffix = "_CONCAT_TABLE_AS";

    /**
     * 根据表名生成类名.
     *
     * @param tableName 表名称
     * @return 类名
     */
    static String toClassName(String tableName) {
        String className = JavaNamingUtils.toCamelCase(tableName, Constant.underscore);
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
        String packageName = PackageNameUtil.getEntityPackageName(userSpecifyPackageName, table.id().schema());
        return ClassName.get(packageName, className);
    }

    /**
     * TABLE field的名称.
     *
     * @return field name
     */
    static String getTableFieldName() {
        return "TABLE";
    }

    /**
     * SCHEMA field的名称.
     *
     * @return field name
     */
    static String getSchemaFieldName() {
        return "SCHEMA";
    }

    /**
     * SCHEMA DOT TABLE field的名称.
     *
     * @return field name
     */
    static String getSchemaDotTableFieldName() {
        return "SCHEMA_DOT_TABLE";
    }

    static String getSchemaDotTableFieldValue(Table table) {
        return table.id().toQuotedString('`');
    }

    /**
     * auto-increment column.
     *
     * @return field name
     */
    static String getAutoIncrementColumn() {
        return "AUTO_INCREMENT_COLUMN";
    }

    /**
     * SCHEMA DOT TABLE field的名称.
     *
     * @return field name
     */
    static String getPrimaryKeyFieldName() {
        return "PRIMARY_KEY";
    }

    /**
     * 列名对应的属性名称.用于{@link TableMetaInfoGenerator}
     *
     * @param columnName 列名称
     * @return 属性名称
     */
    static String toFieldName(String columnName) {
        return columnName.toUpperCase();
    }

    /**
     * 列名称对应的"AS"名称.sql 语法的AS.
     *
     * @param fieldName 列名称
     * @return 对应的AS名称
     */
    static String toAsFieldName(String fieldName) {
        String _fieldName = fieldName.toUpperCase();
        return StringUtils.addSuffix(_fieldName, column_name_as_suffix);
    }

    /**
     * 列名称对应的"AS"名称.sql 语法的AS.与表名称结合.
     *
     * @param fieldName 列名称
     * @return 对应的AS名称
     */
    static String toAsFieldNameConcatTable(String fieldName) {
        String _fieldName = fieldName.toUpperCase();
        return StringUtils.addSuffix(_fieldName, column_name_concat_table_as_suffix);
    }

    /**
     * sql 语法的"AS".
     *
     * @param columnName column name
     * @return field value
     */
    static String getAsFieldValue(String columnName) {
        return JavaNamingUtils.toCamelCase(columnName, Constant.underscore);
    }

    /**
     * sql 语法的"AS".和表名结合.
     *
     * @param tableName  表名称
     * @param columnName column name
     * @return field value
     */
    static String getAsFieldValueConcatTableName(String tableName, String columnName) {
        String alias = tableName + Constant.underscore + columnName;
        return JavaNamingUtils.toCamelCase(alias, Constant.underscore);
    }

    /**
     * 获取表的别名.
     *
     * @param table
     * @return table alias
     */
    static String tableAlias(Table table) {
        String tableAcronym = StringUtils.acronym(table.id().table(), Constant.underscore);
        if (tableAcronym.length() == 1) {
            // 表名只有一个单词
            tableAcronym = table.id().table();
        }
        return table.id().schema() + "_" + tableAcronym;
    }

}
