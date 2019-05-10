package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.ColumnUtils;
import com.wuda.yhan.code.generator.lang.relational.Index;
import com.wuda.yhan.code.generator.lang.relational.Table;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成表对应的mybatis mapper接口.
 *
 * @author wuda
 */
public class MyBatisMapperGenerator {

    /**
     * 生成java class文件.
     *
     * @param table       表的基本信息
     * @param packageName 生成的类所属的包
     * @return java file
     */
    public JavaFile genJavaFile(Table table, String packageName) {
        String className = MyBatisMapperGeneratorUtil.toClassName(table.id().table());
        TypeSpec.Builder classBuilder = TypeSpec.interfaceBuilder(className);
        classBuilder.addAnnotation(MybatisFrameworkUtils.genMapperAnnotation());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addMethod(genInsertMethod(table, packageName, false));
        MethodSpec insertUseGeneratedKeys = genInsertMethod(table, packageName, true);
        if (insertUseGeneratedKeys != null) {
            classBuilder.addMethod(insertUseGeneratedKeys);
        }
        classBuilder.addMethod(genBatchInsertMethod(table, packageName));
        MethodSpec batchInsertUseGeneratedKeys = genBatchInsertUseGeneratedKeysMethod(table, packageName);
        if (batchInsertUseGeneratedKeys != null) {
            classBuilder.addMethod(batchInsertUseGeneratedKeys);
        }
        classBuilder.addMethod(genDeleteByPrimaryKeyMethod(table, packageName));
        classBuilder.addMethod(genUpdateByPrimaryKeyMethod(table, packageName));
        classBuilder.addMethod(genSelectByPrimaryKeyMethod(table, packageName));
        classBuilder.addMethod(genBatchSelectMethod(table, table.primaryKeyColumns(), true, packageName));
        Iterable<MethodSpec> selectByIndexMethods = genSelectByIndexMethod(table, packageName);
        if (selectByIndexMethods != null) {
            classBuilder.addMethods(selectByIndexMethods);
        }
        String finalPackageName = PackageNameUtil.getMapperPackageName(packageName, table.id().schema());
        return JavaFile.builder(finalPackageName, classBuilder.build()).build();
    }

    /**
     * 生成insert方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @param useGeneratedKeys       对于自增的主键,insert后是否返回主键值
     * @return insert method
     */
    private MethodSpec genInsertMethod(Table table, String userSpecifyPackageName, boolean useGeneratedKeys) {
        String methodName;
        if (useGeneratedKeys) {
            if (ColumnUtils.getAutoIncrementColumn(table) == null) {
                return null;
            }
            methodName = Constant.MAPPER_INSERT_USE_GENERATED_KEYS;
        } else {
            methodName = Constant.MAPPER_INSERT;
        }
        ParameterSpec parameterSpec = EntityGeneratorUtil.getEntityParameter(table, userSpecifyPackageName);
        AnnotationSpec insertProviderAnnotationSpec = MybatisFrameworkUtils.getInsertProviderAnnotationSpec(SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName),
                Constant.MAPPER_INSERT);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        builder.addAnnotation(insertProviderAnnotationSpec);
        if (useGeneratedKeys) {
            builder.addAnnotation(useGeneratedKeysAnnotationSpec(table));
        }
        builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameter(parameterSpec);
        return builder.build();
    }

    /**
     * 生成Batch Insert UseGeneratedKeys方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return insert method
     */
    private MethodSpec genBatchInsertUseGeneratedKeysMethod(Table table, String userSpecifyPackageName) {
        if (ColumnUtils.getAutoIncrementColumn(table) == null) {
            return null;
        }
        String methodName = Constant.MAPPER_BATCH_INSERT_USE_GENERATED_KEYS;
        ParameterSpec parameterSpec = EntityGeneratorUtil.getEntityListParameter(table, userSpecifyPackageName);
        AnnotationSpec insertProviderAnnotationSpec = MybatisFrameworkUtils.getInsertProviderAnnotationSpec(SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName),
                methodName);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        builder.addJavadoc("对于自增的主键,insert后返回主键值." +
                "\r\n<strong>注意</strong>,如果某一条准备插入的记录的<i>AUTO_INCREMENT</i>列设置了值" +
                "\r\n,即不使用数据库自增的值" +
                "\r\n,那么取回的自增值会出现混乱.因此此方法强制<i>AUTO_INCREMENT</i>列不能设置值." +
                "\r\n{@link org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator#processBatch}." +
                "\r\n<a href=\"https://dev.mysql.com/doc/refman/8.0/en/information-functions.html#function_last-insert-id\">last_insert_id</a>");
        builder.addAnnotation(insertProviderAnnotationSpec);
        builder.addAnnotation(useGeneratedKeysAnnotationSpec(table));
        builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameter(parameterSpec);
        return builder.build();
    }

    /**
     * Mybatis useGeneratedKeys.
     *
     * @param table table
     * @return useGeneratedKeys相关的注解
     */
    private AnnotationSpec useGeneratedKeysAnnotationSpec(Table table) {
        Column autoIncrementColumn = ColumnUtils.getAutoIncrementColumn(table);
        return MybatisFrameworkUtils.getKeyGenerateAnnotationSpec(true, autoIncrementColumn.name());
    }

    /**
     * 生成批量insert方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return batch insert method
     */
    private MethodSpec genBatchInsertMethod(Table table, String userSpecifyPackageName) {
        String methodName = Constant.MAPPER_BATCH_INSERT;
        ParameterSpec parameterSpec = EntityGeneratorUtil.getEntityListParameter(table, userSpecifyPackageName);
        AnnotationSpec insertAnnotationSpec = MybatisFrameworkUtils.getInsertAnnotationSpec(batchInsertScript(table));
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        builder.addAnnotation(insertAnnotationSpec);
        builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameter(parameterSpec);
        return builder.build();
    }

    /**
     * Mybatis batch insert sql语句.
     *
     * @param table 数据库表
     * @return 批量插入方法的sql
     */
    private String batchInsertScript(Table table) {
        String schemaDotTable = PackageNameUtil.getSchemaDotTable(table);
        StringBuilder builder = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        builder.append("<script>");
        builder.append(newLine);
        builder.append("INSERT INTO ").append(schemaDotTable);

        List<Column> tableColumns = table.columns();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        String item = "entity";
        String columnName;
        for (Column column : tableColumns) {
            columnName = column.name();
            columns.append(columnName).append(",");
            values.append("#{").append(item).append(".").append(EntityGeneratorUtil.toFieldName(columnName)).append("},");
        }

        builder.append("(").append(columns.substring(0, columns.length() - 1)).append(")");
        String collection = MyBatisMapperGeneratorUtil.getListParamName();
        builder.append(newLine);
        builder.append(" VALUES");
        builder.append(newLine);
        builder.append("<foreach item='").append(item).append("' collection='").append(collection).append("' open='' separator=',' close=''>");
        builder.append(newLine);
        builder.append("(").append(values.substring(0, values.length() - 1)).append(")");
        builder.append(newLine);
        builder.append("</foreach>");
        builder.append(newLine);
        builder.append("</script>");
        return builder.toString();
    }

    /**
     * 生成delete by primary key方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return method
     */
    private MethodSpec genDeleteByPrimaryKeyMethod(Table table, String userSpecifyPackageName) {
        AnnotationSpec annotationSpec = MybatisFrameworkUtils.getDeleteProviderAnnotationSpec(SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName),
                Constant.MAPPER_DELETE_BY_PRIMARY_KEY);
        return MethodSpec.methodBuilder(Constant.MAPPER_DELETE_BY_PRIMARY_KEY)
                .addAnnotation(annotationSpec)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameters(MyBatisMapperGeneratorUtil.getPrimaryKeyParameterSpec(table, true))
                .build();
    }

    /**
     * 生成update方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return update method
     */
    private MethodSpec genUpdateByPrimaryKeyMethod(Table table, String userSpecifyPackageName) {
        String methodName = Constant.MAPPER_UPDATE_BY_PRIMARY_KEY;
        ParameterSpec parameterSpec = EntityGeneratorUtil.getEntityParameter(table, userSpecifyPackageName);
        AnnotationSpec annotationSpec = MybatisFrameworkUtils.getUpdateProviderAnnotationSpec(SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName),
                Constant.MAPPER_UPDATE_BY_PRIMARY_KEY);
        return MethodSpec.methodBuilder(methodName)
                .addAnnotation(annotationSpec)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameter(parameterSpec)
                .build();
    }

    /**
     * 生成select by primary key方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return method
     */
    private MethodSpec genSelectByPrimaryKeyMethod(Table table, String userSpecifyPackageName) {
        return genSelectMethod(table, table.primaryKeyColumns(), true, false, userSpecifyPackageName);
    }

    /**
     * 具体查看{@link SqlBuilderGenerator#genSelectByIndexMethodSpec(Table, String)}定义.
     *
     * @param table                  table
     * @param userSpecifyPackageName user specify package name
     * @return list of {@link MethodSpec}
     */
    private Iterable<MethodSpec> genSelectByIndexMethod(Table table, String userSpecifyPackageName) {
        List<Index> indices = table.getIndices();
        if (indices == null || indices.isEmpty()) {
            return null;
        }
        List<MethodSpec> methods = new ArrayList<>(indices.size());
        for (Index index : indices) {
            List<Column> indexColumns = ColumnUtils.indexColumns(table, index);
            MethodSpec methodSpec = genSelectMethod(table, indexColumns, false, index.getType() == Index.Type.UNIQUE, userSpecifyPackageName);
            methods.add(methodSpec);
            if (index.getType() != Index.Type.UNIQUE) {
                MethodSpec selectCountMethodSpec = genSelectCountMethod(table, indexColumns, userSpecifyPackageName);
                methods.add(selectCountMethodSpec);
            } else {
                MethodSpec batchSelectMethod = genBatchSelectMethod(table, indexColumns, false, userSpecifyPackageName);
                methods.add(batchSelectMethod);
            }
        }
        return methods;
    }

    /**
     * 具体查看{@link SqlBuilderGenerator#genSelectMethod(Table, List, boolean, boolean, String)}方法的定义.
     *
     * @param table                  table
     * @param whereClauseColumns     where clause columns
     * @param primaryKey             is primary key
     * @param uniqueIndex            is unique index
     * @param userSpecifyPackageName user specify package name
     * @return {@link MethodSpec}
     * @see SqlBuilderGenerator#genSelectMethod(Table, List, boolean, boolean, String)
     */
    private MethodSpec genSelectMethod(Table table, List<Column> whereClauseColumns, boolean primaryKey, boolean uniqueIndex, String userSpecifyPackageName) {
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getSelectMethodName(columnNames, primaryKey);
        TypeName sqlBuilderType = SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName);
        AnnotationSpec sqlBuilderAnnotation = MybatisFrameworkUtils.getSelectProviderAnnotationSpec(sqlBuilderType, methodName);
        TypeName returns;
        Iterable<ParameterSpec> pagingParameterSpecs = null;
        if (primaryKey || uniqueIndex) {
            returns = EntityGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        } else {
            returns = EntityGeneratorUtil.listOfTableEntity(table, userSpecifyPackageName);
            pagingParameterSpecs = MyBatisMapperGeneratorUtil.getPagingParameterSpecs(true);
        }
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addAnnotation(sqlBuilderAnnotation)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returns)
                .addParameters(MyBatisMapperGeneratorUtil.getParameterSpecs(whereClauseColumns, true));
        if (pagingParameterSpecs != null) {
            builder.addParameters(pagingParameterSpecs);
        }
        builder.addParameter(MyBatisMapperGeneratorUtil.getRetrieveColumnsParameterSpec(true));
        return builder.build();
    }

    /**
     * 生成<code>SELECT COUNT</code>方法,用于获取总数.
     * 具体查看{@link SqlBuilderGenerator#genSelectCountMethod(Table, List, String)}方法的定义.
     *
     * @param table                  table
     * @param whereClauseColumns     where clause columns
     * @param userSpecifyPackageName user specify package name
     * @return {@link MethodSpec}
     */
    private MethodSpec genSelectCountMethod(Table table, List<Column> whereClauseColumns, String userSpecifyPackageName) {
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getSelectCountMethodName(columnNames);
        TypeName sqlBuilderType = SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName);
        AnnotationSpec sqlBuilderAnnotation = MybatisFrameworkUtils.getSelectProviderAnnotationSpec(sqlBuilderType, methodName);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addAnnotation(sqlBuilderAnnotation)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(Integer.TYPE)
                .addParameters(MyBatisMapperGeneratorUtil.getParameterSpecs(whereClauseColumns, true));
        return builder.build();
    }

    /**
     * 生成批量查询方法.
     * 具体查看{@link SqlBuilderGenerator#genBatchSelectMethod(Table, List, boolean, String)}方法的定义.
     *
     * @param table                  table
     * @param whereClauseColumns     where clause columns
     * @param primaryKey             是否主键
     * @param userSpecifyPackageName user specify package name
     * @return 方法定义
     */
    private MethodSpec genBatchSelectMethod(Table table, List<Column> whereClauseColumns, boolean primaryKey, String userSpecifyPackageName) {
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getBatchSelectMethodName(columnNames, primaryKey);
        TypeName sqlBuilderType = SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName);
        AnnotationSpec sqlBuilderAnnotation = MybatisFrameworkUtils.getSelectProviderAnnotationSpec(sqlBuilderType, methodName);
        ParameterizedTypeName returns = EntityGeneratorUtil.listOfTableEntity(table, userSpecifyPackageName);
        ParameterSpec parameterSpec = MyBatisMapperGeneratorUtil.getBatchSelectParameterSpec(whereClauseColumns, true, table, userSpecifyPackageName);
        ParameterSpec retrieveColumns = MyBatisMapperGeneratorUtil.getRetrieveColumnsParameterSpec(true);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addAnnotation(sqlBuilderAnnotation)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returns)
                .addParameter(parameterSpec)
                .addParameter(retrieveColumns);
        return builder.build();
    }
}
