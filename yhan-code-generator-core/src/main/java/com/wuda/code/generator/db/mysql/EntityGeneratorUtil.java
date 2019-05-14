package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.code.generator.TypeNameUtils;
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
    static String toClassName(String tableName) {
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
    static String toFieldName(String columnName) {
        return JavaNamingUtil.toCamelCase(columnName, Constant.word_separator);
    }

    /**
     * 根据列名生成字段名称.
     *
     * @param columnName 列名称
     * @return 属性名称
     */
    static String toIsSetFieldName(String columnName) {
        String fieldName = JavaNamingUtil.toCamelCase(columnName, Constant.word_separator);
        return StringUtil.addSuffix(fieldName, IsSetField.suffix);
    }

    /**
     * insert,update方法通常是用表对应的实体作为参数.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @param mybatisParamAnnotation 是否需要在参数前添加{@link Param}注解
     * @return 参数信息
     */
    static ParameterSpec getEntityParameter(Table table, String userSpecifyPackageName, boolean mybatisParamAnnotation) {
        String entityName = EntityGeneratorUtil.toClassName(table.id().table());
        String parameterName = StringUtil.firstCharToLowerCase(entityName);
        TypeName typeName = getTypeName(table, userSpecifyPackageName);
        ParameterSpec.Builder builder = ParameterSpec.builder(typeName, parameterName);
        if (mybatisParamAnnotation) {
            builder.addAnnotation(MybatisFrameworkUtils.getParamAnnotationSpec(parameterName));
        }
        return builder.build();
    }

    /**
     * 批量insert,update方法通常是用表对应的实体的集合作为参数.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return 参数信息
     */
    static ParameterSpec getEntityListParameter(Table table, String userSpecifyPackageName) {
        ParameterizedTypeName listOfTableEntity = listOfTableEntity(table, userSpecifyPackageName);
        String parameterName = MyBatisMapperGeneratorUtil.getListParamName();
        AnnotationSpec paramAnnotationSpec = MybatisFrameworkUtils.getParamAnnotationSpec(parameterName);
        return ParameterSpec.builder(listOfTableEntity, parameterName)
                .addAnnotation(paramAnnotationSpec)
                .build();
    }

    /**
     * {@link ParameterizedTypeName},List的泛型类型是当前表对应的实体.
     * 比如: Table是User,则返回List<User>.
     *
     * @param table                  table
     * @param userSpecifyPackageName 包名
     * @return 泛型类型的{@link {@link TypeName}}
     */
    static ParameterizedTypeName listOfTableEntity(Table table, String userSpecifyPackageName) {
        TypeName typeName = getTypeName(table, userSpecifyPackageName);
        ClassName list = TypeNameUtils.listClassName();
        return ParameterizedTypeName.get(list, typeName);
    }

    /**
     * {@link TypeName}.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return {@link TypeName}
     */
    static TypeName getTypeName(Table table, String userSpecifyPackageName) {
        String packageName = PackageNameUtil.getEntityPackageName(userSpecifyPackageName, table.id().schema());
        String entityClassName = EntityGeneratorUtil.toClassName(table.id().table());
        return ClassName.get(packageName, entityClassName);
    }
}
