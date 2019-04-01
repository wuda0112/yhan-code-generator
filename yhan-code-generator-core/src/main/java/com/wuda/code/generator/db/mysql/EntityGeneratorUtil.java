package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.relational.Table;
import com.wuda.yhan.util.commons.IsSetField;
import com.wuda.yhan.util.commons.JavaNamingUtil;
import com.wuda.yhan.util.commons.StringUtil;
import org.apache.ibatis.annotations.Param;

/**
 * {@link EntityGenerator}生成代码时,命名工具类.
 *
 * @author wuda
 */
class EntityGeneratorUtil {

    /**
     * 根据表名生成类名.
     *
     * @param tableName 表名称
     * @return 类名
     */
    static String genClassName(String tableName) {
        String className = JavaNamingUtil.toCamelCase(tableName, Constant.word_separator);
        className = StringUtil.firstCharToUpperCase(className);
        return className;
    }

    /**
     * 根据列名生成字段名称.
     *
     * @param columnName 列名称
     * @return 属性名称
     */
    static String genFieldName(String columnName) {
        return JavaNamingUtil.toCamelCase(columnName, Constant.word_separator);
    }

    /**
     * 根据列名生成字段名称.
     *
     * @param columnName 列名称
     * @return 属性名称
     */
    static String genIsSetFieldName(String columnName) {
        String fieldName = JavaNamingUtil.toCamelCase(columnName, Constant.word_separator);
        return StringUtil.addSuffix(fieldName, IsSetField.suffix);
    }

    /**
     * insert,update方法通常是用表对应的实体作为参数.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return 参数信息
     */
    static ParameterSpec genEntityParameter(Table table, String userSpecifyPackageName) {
        String entityName = EntityGeneratorUtil.genClassName(table.id().table());
        String parameterName = StringUtil.firstCharToLowerCase(entityName);
        TypeName typeName = genEntityClassName(table, userSpecifyPackageName);
        return ParameterSpec.builder(typeName, parameterName).build();
    }

    /**
     * 批量insert,update方法通常是用表对应的实体的集合作为参数.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return 参数信息
     */
    static ParameterSpec genEntityListParameter(Table table, String userSpecifyPackageName) {
        TypeName typeName = genEntityClassName(table, userSpecifyPackageName);
        ClassName list = ClassName.get("java.util", "List");
        TypeName listOfEntities = ParameterizedTypeName.get(list, typeName);
        String parameterName = MyBatisMapperGeneratorUtil.getBatchInsertParamName();
        AnnotationSpec paramAnnotationSpec = AnnotationSpec.builder(Param.class)
                .addMember("value", "$S", parameterName)
                .build();
        return ParameterSpec.builder(listOfEntities, parameterName)
                .addAnnotation(paramAnnotationSpec)
                .build();
    }

    /**
     * ClassName.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return ClassName
     */
    static ClassName genEntityClassName(Table table, String userSpecifyPackageName) {
        String packageName = PackageNameUtil.getPackageName(userSpecifyPackageName, table.id().schema());
        String entityClassName = EntityGeneratorUtil.genClassName(table.id().table());
        return ClassName.get(packageName, entityClassName);
    }
}
