package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.code.generator.TypeNameUtils;
import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.util.JavaNamingUtils;
import com.wuda.yhan.code.generator.lang.util.StringUtils;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Table;
import org.apache.ibatis.annotations.Param;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.wuda.code.generator.db.mysql.MybatisFrameworkUtils.getParamAnnotationSpec;

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
        String className = JavaNamingUtils.toCamelCase(tableName, Constant.word_separator);
        className = StringUtils.firstCharToUpperCase(className);
        className = StringUtils.addSuffix(className, Constant.MAPPER_CLASS_NAME_SUFFIX);
        return className;
    }

    /**
     * 获取list参数对应的名称.
     *
     * @return param name
     */
    static String getListParamName() {
        return "list";
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
                builder.addAnnotation(getParamAnnotationSpec(parameterName));
            }
            list.add(builder.build());
        }
        return list;
    }

    /**
     * 适用batch select方法的输入参数.
     *
     * @param columns                column
     * @param mybatisParamAnnotation 参数上是否加上{@link Param}注解
     * @return 输入参数定义
     */
    static ParameterSpec getBatchSelectParameterSpec(List<Column> columns, boolean mybatisParamAnnotation, Table table, String userSpecifyPackageName) {
        String parameterName = getListParamName();
        ParameterizedTypeName parameterizedTypeName;
        if (columns.size() == 1) {
            Column column = columns.get(0);
            Class<?> dataType = MysqlTypeUtil.mysqlTypeToJavaType(column.typeExpression());
            parameterizedTypeName = TypeNameUtils.listOf(dataType);
        } else {
            parameterizedTypeName = EntityGeneratorUtil.listOfTableEntity(table, userSpecifyPackageName);
        }
        ParameterSpec.Builder builder = ParameterSpec.builder(parameterizedTypeName, parameterName);
        if (mybatisParamAnnotation) {
            builder.addAnnotation(getParamAnnotationSpec(parameterName));
        }
        return builder.build();
    }

    /**
     * select方法的返回列参数.
     *
     * @param mybatisParamAnnotation 是否需要在参数前添加{@link Param}注解
     * @return ParameterSpec
     */
    static ParameterSpec getRetrieveColumnsParameterSpec(boolean mybatisParamAnnotation) {
        String parameterName = Constant.RETRIEVE_COLUMNS;
        ArrayTypeName arrayTypeName = ArrayTypeName.of(String.class);
        ParameterSpec.Builder builder = ParameterSpec.builder(arrayTypeName, parameterName);
        if (mybatisParamAnnotation) {
            builder.addAnnotation(MybatisFrameworkUtils.getParamAnnotationSpec(parameterName));
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
            builder.addAnnotation(getParamAnnotationSpec(parameterName));
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
            builder.addAnnotation(getParamAnnotationSpec(parameterName));
        }
        return builder.build();
    }

    /**
     * 根据<i>WHERE</i>条件中的列生成方法名.
     *
     * @param whereClauseColumns where条件中的所有column
     * @param primaryKey         这些column是否组成主键
     * @param forUpdate          SELECT ... FOR UPDATE
     * @return 对应的方法名
     */
    static String getSelectMethodName(List<String> whereClauseColumns, boolean primaryKey, boolean forUpdate) {
        StringBuilder methodName = new StringBuilder();
        if (primaryKey) {
            methodName.append(Constant.MAPPER_SELECT_BY_PRIMARY_KEY);
        } else {
            methodName.append(Constant.SELECT_BY_PREFIX);
            andSeparated(whereClauseColumns, methodName);
        }
        if (forUpdate) {
            methodName.append(Constant.FOR_UPDATE_SUFFIX);
        }
        return methodName.toString();
    }

    /**
     * 根据<i>WHERE</i>条件中的列生成方法名.
     *
     * @param whereClauseColumns where条件中的所有column
     * @param primaryKey         这些column是否组成主键
     * @return 对应的方法名
     */
    static String getBatchSelectMethodName(List<String> whereClauseColumns, boolean primaryKey) {
        if (primaryKey) {
            return Constant.MAPPER_BATCH_SELECT_BY_PRIMARY_KEY;
        }
        StringBuilder builder = new StringBuilder(Constant.BATCH_SELECT_BY_PREFIX);
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
        StringBuilder builder = new StringBuilder(Constant.COUNT_BY_PREFIX);
        andSeparated(whereClauseColumns, builder);
        return builder.toString();
    }

    /**
     * 根据<i>WHERE</i>条件中的列生成方法名.
     *
     * @param whereClauseColumns where条件中的所有column
     * @param primaryKey         这些column是否组成主键
     * @return 对应的方法名
     */
    static String getUpdateMethodName(List<String> whereClauseColumns, boolean primaryKey) {
        StringBuilder methodName = new StringBuilder();
        if (primaryKey) {
            methodName.append(Constant.MAPPER_UPDATE_BY_PRIMARY_KEY);
        } else {
            methodName.append(Constant.UPDATE_BY_PREFIX);
            andSeparated(whereClauseColumns, methodName);
        }
        return methodName.toString();
    }

    /**
     * 根据<i>WHERE</i>条件中的列生成方法名.
     *
     * @param whereClauseColumns where条件中的所有column
     * @param primaryKey         这些column是否组成主键
     * @return 对应的方法名
     */
    static String getDeleteMethodName(List<String> whereClauseColumns, boolean primaryKey) {
        StringBuilder methodName = new StringBuilder();
        if (primaryKey) {
            methodName.append(Constant.MAPPER_DELETE_BY_PRIMARY_KEY);
        } else {
            methodName.append(Constant.DELETE_BY_PREFIX);
            andSeparated(whereClauseColumns, methodName);
        }
        return methodName.toString();
    }

    private static void andSeparated(List<String> whereClauseColumns, StringBuilder appender) {
        int last = whereClauseColumns.size() - 1;
        String column;
        for (int index = 0; index < whereClauseColumns.size(); index++) {
            column = whereClauseColumns.get(index);
            String fieldName = EntityGeneratorUtil.toFieldName(column);
            fieldName = StringUtils.firstCharToUpperCase(fieldName);
            appender.append(fieldName);
            if (index != last) {
                appender.append("And");
            }
        }
    }

    /**
     * where clause provider.
     *
     * @param mybatisParamAnnotation 是否需要在参数前添加{@link Param}注解
     * @return ParameterSpec
     */
    static ParameterSpec getWhereClauseProviderParameterSpec(boolean mybatisParamAnnotation) {
        String parameterName = Constant.WHERE_CLAUSE_PROVIDER;
        ClassName parameterType = ClassName.get(WhereClauseProvider.class);
        ParameterSpec.Builder builder = ParameterSpec.builder(parameterType, parameterName);
        if (mybatisParamAnnotation) {
            builder.addAnnotation(getParamAnnotationSpec(parameterName));
        }
        return builder.build();
    }
}
