package com.wuda.code.generator.db.mysql;

import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.util.commons.JavaNamingUtil;
import com.wuda.yhan.util.commons.StringUtil;

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
     * @param tableName
     *         表名称
     * @return 类名
     */
    static String genClassName(String tableName) {
        String className = JavaNamingUtil.toCamelCase(tableName, Constant.word_separator);
        className = StringUtil.firstCharToUpperCase(className);
        className = StringUtil.addSuffix(className, class_name_suffix);
        return className;
    }

    /**
     * TABLE_NAME field的名称.
     *
     * @return field name
     */
    static String getTableNameFieldName() {
        return "TABLE_NAME";
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
     * 列名对应的属性名称.用于{@link TableMetaInfoGenerator}
     *
     * @param columnName
     *         列名称
     * @return 属性名称
     */
    static String genFieldName(String columnName) {
        return columnName.toUpperCase();
    }

    /**
     * 列名称对应的"AS"名称.sql 语法的AS.
     *
     * @param fieldName
     *         列名称
     * @return 对应的AS名称
     */
    static String genAsFieldName(String fieldName) {
        String _fieldName = fieldName.toUpperCase();
        return StringUtil.addSuffix(_fieldName, column_name_as_suffix);
    }

    /**
     * 列名称对应的"AS"名称.sql 语法的AS.与表名称结合.
     *
     * @param fieldName
     *         列名称
     * @return 对应的AS名称
     */
    static String genAsFieldNameConcatTable(String fieldName) {
        String _fieldName = fieldName.toUpperCase();
        return StringUtil.addSuffix(_fieldName, column_name_concat_table_as_suffix);
    }

    /**
     * sql 语法的"AS".
     *
     * @param columnName
     *         column name
     * @return field value
     */
    static String getAsFieldValue(String columnName) {
        return columnName + " AS " + JavaNamingUtil.toCamelCase(columnName, Constant.word_separator);
    }

    /**
     * sql 语法的"AS".和表名结合.
     *
     * @param tableName
     *         表名称
     * @param columnName
     *         column name
     * @return field value
     */
    static String getAsFieldValueConcatTableName(String tableName, String columnName) {
        String alias = tableName + Constant.word_separator + columnName;
        return columnName + " AS " + JavaNamingUtil.toCamelCase(alias, Constant.word_separator);
    }

}
