package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ParameterSpec;
import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Table;
import com.wuda.yhan.util.commons.JavaNamingUtil;
import com.wuda.yhan.util.commons.StringUtil;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link MyBatisMapperGenerator}生成代码时,命名工具类.
 *
 * @author wuda
 */
class MyBatisMapperGeneratorUtil {

    /**
     * 根据表名生成类名.
     *
     * @param tableName 表名称
     * @return 类名
     */
    static String genClassName(String tableName) {
        String className = JavaNamingUtil.toCamelCase(tableName, Constant.word_separator);
        className = StringUtil.firstCharToUpperCase(className);
        className = StringUtil.addSuffix(className, Constant.MAPPER_CLASS_NAME_SUFFIX);
        return className;
    }

    /**
     * 获取insert方法的名称.
     *
     * @return insert method name
     */
    static String getInsertMethodName() {
        return Constant.MAPPER_INSERT;
    }

    /**
     * 获取batch insert方法的名称.
     *
     * @return batch insert method name
     */
    static String getBatchInsertMethodName() {
        return Constant.MAPPER_BATCH_INSERT;
    }

    /**
     * 获取batch insert方法参数的名称.
     *
     * @return batch insert method param name
     */
    static String getBatchInsertParamName() {
        return "list";
    }

    /**
     * 获取delete方法的名称.
     *
     * @return delete method name
     */
    static String getDeleteByPrimaryKeyMethodName() {
        return Constant.MAPPER_DELETE_BY_PRIMARY_KEY;
    }

    /**
     * 获取update方法的名称.
     *
     * @return update method name
     */
    static String getUpdateByPrimaryKeyMethodName() {
        return Constant.MAPPER_UPDATE_BY_PRIMARY_KEY;
    }

    /**
     * 获取select方法的名称.
     *
     * @return select method name
     */
    static String getSelectByPrimaryKeyMethodName() {
        return Constant.MAPPER_SELECT_BY_PRIMARY_KEY;
    }

    /**
     * 主键中的字段生成参数.
     *
     * @param table                  table
     * @param mybatisParamAnnotation 是否在参数前添加{@link org.apache.ibatis.annotations.Param}注解
     * @return 主键中可能是多个column
     */
    static Iterable<ParameterSpec> genPrimaryKeyParameter(Table table, boolean mybatisParamAnnotation) {
        List<Column> primaryKeyColumns = table.primaryKeyColumns();
        List<ParameterSpec> list = new ArrayList<>(primaryKeyColumns.size());
        for (Column column : primaryKeyColumns) {
            Class<?> type = MysqlTypeUtil.mysqlTypeToJavaType(column.typeExpression());
            String parameterName = EntityGeneratorUtil.genFieldName(column.name());
            ParameterSpec.Builder builder = ParameterSpec.builder(type, parameterName);
            if (mybatisParamAnnotation) {
                builder.addAnnotation(genMybatisParamAnnotion(parameterName));
            }
            list.add(builder.build());
        }
        return list;
    }

    /**
     * 生成{@link Param}注解.
     *
     * @param parameterName parameter name
     * @return {@link Param}注解
     */
    static AnnotationSpec genMybatisParamAnnotion(String parameterName) {
        return AnnotationSpec.builder(Param.class)
                .addMember("value", "$S", parameterName)
                .build();
    }

    /**
     * select方法的返回列参数.
     *
     * @param mybatisParamAnnotation 是否需要在参数前添加{@link Param}注解
     * @return ParameterSpec
     */
    static ParameterSpec genRetrieveColumnsParam(boolean mybatisParamAnnotation) {
        String parameterName = "columns";
        ArrayTypeName arrayTypeName = ArrayTypeName.of(String.class);
        ParameterSpec.Builder builder = ParameterSpec.builder(arrayTypeName, parameterName);
        if (mybatisParamAnnotation) {
            builder.addAnnotation(genMybatisParamAnnotion(parameterName));
        }
        return builder.build();
    }
}
