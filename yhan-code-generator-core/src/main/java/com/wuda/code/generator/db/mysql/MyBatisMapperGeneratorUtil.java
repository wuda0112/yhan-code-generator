package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Table;
import com.wuda.yhan.util.commons.JavaNamingUtil;
import com.wuda.yhan.util.commons.StringUtil;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.wuda.code.generator.db.mysql.MybatisFrameworkUtils.getMybatisParamAnnotationSpec;

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
    static String toClassName(String tableName) {
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
     * 主键中的列生成方法的输入参数.
     *
     * @param table                  table
     * @param mybatisParamAnnotation 是否在参数前添加{@link org.apache.ibatis.annotations.Param}注解
     * @return 输入参数定义
     */
    static Iterable<ParameterSpec> getPrimaryKeyParameterSpec(Table table, boolean mybatisParamAnnotation) {
        List<Column> primaryKeyColumns = table.primaryKeyColumns();
        return getParameterSpecs(primaryKeyColumns, mybatisParamAnnotation);
    }

    /**
     * 用给定的列生成方法的输入参数.
     *
     * @param columns                column
     * @param mybatisParamAnnotation 参数上是否加上{@link Param}注解
     * @return 输入参数定义
     */
    static Iterable<ParameterSpec> getParameterSpecs(List<Column> columns, boolean mybatisParamAnnotation) {
        List<ParameterSpec> list = new ArrayList<>(columns.size());
        for (Column column : columns) {
            Class<?> type = MysqlTypeUtil.mysqlTypeToJavaType(column.typeExpression());
            String parameterName = EntityGeneratorUtil.toFieldName(column.name());
            ParameterSpec.Builder builder = ParameterSpec.builder(type, parameterName);
            if (mybatisParamAnnotation) {
                builder.addAnnotation(getMybatisParamAnnotationSpec(parameterName));
            }
            list.add(builder.build());
        }
        return list;
    }

    /**
     * select方法的返回列参数.
     *
     * @param mybatisParamAnnotation 是否需要在参数前添加{@link Param}注解
     * @return ParameterSpec
     */
    static ParameterSpec getRetrieveColumnsParameterSpec(boolean mybatisParamAnnotation) {
        String parameterName = "retrieveColumns";
        ArrayTypeName arrayTypeName = ArrayTypeName.of(String.class);
        ParameterSpec.Builder builder = ParameterSpec.builder(arrayTypeName, parameterName);
        if (mybatisParamAnnotation) {
            builder.addAnnotation(MybatisFrameworkUtils.getMybatisParamAnnotationSpec(parameterName));
        }
        return builder.build();
    }

    /**
     * 分页参数.
     *
     * @param mybatisParamAnnotation 是否需要在参数前添加{@link Param}注解
     * @return ParameterSpec
     */
    static Iterable<ParameterSpec> getPagingParameterSpecs(boolean mybatisParamAnnotation) {
        return Arrays.asList(getPagingOffsetParameterSpec(mybatisParamAnnotation), getPagingRowCountParameterSpec(mybatisParamAnnotation));
    }

    /**
     * 分页参数的<i>offset</i>.
     *
     * @param mybatisParamAnnotation 是否需要在参数前添加{@link Param}注解
     * @return ParameterSpec
     */
    static ParameterSpec getPagingOffsetParameterSpec(boolean mybatisParamAnnotation) {
        String parameterName = Constant.PAGING_OFFSET;
        ParameterSpec.Builder builder = ParameterSpec.builder(TypeName.INT, parameterName);
        if (mybatisParamAnnotation) {
            builder.addAnnotation(getMybatisParamAnnotationSpec(parameterName));
        }
        return builder.build();
    }

    /**
     * 分页参数的<i>row_count</i>.
     *
     * @param mybatisParamAnnotation 是否需要在参数前添加{@link Param}注解
     * @return ParameterSpec
     */
    static ParameterSpec getPagingRowCountParameterSpec(boolean mybatisParamAnnotation) {
        String parameterName = Constant.PAGING_ROW_COUNT;
        ParameterSpec.Builder builder = ParameterSpec.builder(TypeName.INT, parameterName);
        if (mybatisParamAnnotation) {
            builder.addAnnotation(getMybatisParamAnnotationSpec(parameterName));
        }
        return builder.build();
    }

    /**
     * 根据<i>WHERE</i>条件中的列生成方法名.
     *
     * @param whereClauseColumns where条件中的所有column
     * @param primaryKey         这些column是否组成主键
     * @return 对应的方法名
     */
    static String getSelectMethodName(List<String> whereClauseColumns, boolean primaryKey) {
        if (primaryKey) {
            return MyBatisMapperGeneratorUtil.getSelectByPrimaryKeyMethodName();
        }
        StringBuilder builder = new StringBuilder("selectBy");
        andSeparated(whereClauseColumns, builder);
        return builder.toString();
    }

    /**
     * 根据<i>WHERE</i>条件中的列生成方法名.
     *
     * @param whereClauseColumns where条件中的所有column
     * @return 对应的方法名
     */
    static String getSelectCountMethodName(List<String> whereClauseColumns) {
        StringBuilder builder = new StringBuilder("countBy");
        andSeparated(whereClauseColumns, builder);
        return builder.toString();
    }

    private static void andSeparated(List<String> whereClauseColumns, StringBuilder appender) {
        int last = whereClauseColumns.size() - 1;
        String column;
        for (int index = 0; index < whereClauseColumns.size(); index++) {
            column = whereClauseColumns.get(index);
            String fieldName = EntityGeneratorUtil.toFieldName(column);
            fieldName = StringUtil.firstCharToUpperCase(fieldName);
            appender.append(fieldName);
            if (index != last) {
                appender.append("And");
            }
        }
    }
}
