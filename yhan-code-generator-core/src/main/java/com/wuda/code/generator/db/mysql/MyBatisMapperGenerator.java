package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.yhan.code.generator.lang.util.ColumnUtils;
import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.util.TableUtils;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Index;
import com.wuda.yhan.code.generator.lang.relational.Table;

import javax.lang.model.element.Modifier;
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
        // 主键
        List<Column> primaryKeyColumns = table.primaryKeyColumns();
        classBuilder.addMethod(genDeleteMethod(table, packageName, primaryKeyColumns, true));
        classBuilder.addMethod(genUpdateMethod(table, packageName, primaryKeyColumns, true));
        classBuilder.addMethod(genSelectMethod(table, packageName, primaryKeyColumns, true, false, false));
        classBuilder.addMethod(genSelectMethod(table, packageName, primaryKeyColumns, true, false, true));
        classBuilder.addMethod(genBatchSelectMethod(table, packageName, primaryKeyColumns, true));

        // 唯一索引
        List<Index> uniqueIndices = TableUtils.getUniqueIndices(table);
        if (uniqueIndices != null && !uniqueIndices.isEmpty()) {
            for (Index index : uniqueIndices) {
                List<Column> indexColumns = ColumnUtils.indexColumns(table, index);
                classBuilder.addMethod(genDeleteMethod(table, packageName, indexColumns, false));
                classBuilder.addMethod(genUpdateMethod(table, packageName, indexColumns, false));
                classBuilder.addMethod(genSelectMethod(table, packageName, indexColumns, false, true, false));
                classBuilder.addMethod(genSelectMethod(table, packageName, indexColumns, false, true, true));
                classBuilder.addMethod(genBatchSelectMethod(table, packageName, indexColumns, false));
            }
        }
        // 非唯一索引
        List<Index> nonUniqueIndices = TableUtils.getNonUniqueIndices(table);
        if (nonUniqueIndices != null && !nonUniqueIndices.isEmpty()) {
            for (Index index : nonUniqueIndices) {
                List<Column> indexColumns = ColumnUtils.indexColumns(table, index);
                classBuilder.addMethod(genSelectMethod(table, packageName, indexColumns, false, false, false));
                classBuilder.addMethod(genSelectCountMethod(table, packageName, indexColumns));
            }
        }

        classBuilder.addMethod(genSelectByExampleMethod(table, packageName, true));
        classBuilder.addMethod(genSelectByExampleMethod(table, packageName, false));
        classBuilder.addMethod(genSelectCountByExampleMethod(table, packageName));

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
        ParameterSpec parameterSpec = EntityGeneratorUtil.getEntityParameter(table, userSpecifyPackageName, false);
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
     * 生成delete方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @param whereClauseColumns     删除条件的列
     * @param primaryKey             <i>whereClauseColumns</i>是否主键
     * @return method
     */
    private MethodSpec genDeleteMethod(Table table, String userSpecifyPackageName, List<Column> whereClauseColumns, boolean primaryKey) {
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getDeleteMethodName(columnNames, primaryKey);
        AnnotationSpec annotationSpec = MybatisFrameworkUtils.getDeleteProviderAnnotationSpec(SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName),
                methodName);
        return MethodSpec.methodBuilder(methodName)
                .addAnnotation(annotationSpec)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameters(MyBatisMapperGeneratorUtil.getParameterSpecs(whereClauseColumns, true))
                .build();
    }

    /**
     * 生成update方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @param whereClauseColumns     更新条件的列
     * @param primaryKey             <i>whereClauseColumns</i>是否主键
     * @return update method
     */
    private MethodSpec genUpdateMethod(Table table, String userSpecifyPackageName, List<Column> whereClauseColumns, boolean primaryKey) {
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getUpdateMethodName(columnNames, primaryKey);
        Iterable<ParameterSpec> conditionsParameterSpec = MyBatisMapperGeneratorUtil.getParameterSpecs(whereClauseColumns, true);
        ParameterSpec updateParameterSpec = EntityGeneratorUtil.getEntityParameter(table, userSpecifyPackageName, true);
        AnnotationSpec updateProviderAnnotationSpec = MybatisFrameworkUtils.getUpdateProviderAnnotationSpec(SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName),
                methodName);
        return MethodSpec.methodBuilder(methodName)
                .addAnnotation(updateProviderAnnotationSpec)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameters(conditionsParameterSpec)
                .addParameter(updateParameterSpec)
                .build();
    }

    /**
     * 生成查询方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName user specify package name
     * @param whereClauseColumns     where clause columns
     * @param primaryKey             is primary key
     * @param uniqueIndex            is unique index
     * @param forUpdate              SELECT ... FOR UPDATE
     * @return {@link MethodSpec}
     */
    private MethodSpec genSelectMethod(Table table, String userSpecifyPackageName, List<Column> whereClauseColumns, boolean primaryKey, boolean uniqueIndex, boolean forUpdate) {
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getSelectMethodName(columnNames, primaryKey, forUpdate);
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
     * 自定义sql where的查询方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName user specify package name
     * @param returnOne              <code>true</code>-如果返回结果只有一条记录，<code>false</code>-如果返回结果是<code>list</code>
     * @return {@link MethodSpec}
     */
    private MethodSpec genSelectByExampleMethod(Table table, String userSpecifyPackageName, boolean returnOne) {
        String methodName;
        if (returnOne) {
            methodName = Constant.SELECT_ONE_BY_EXAMPLE;
        } else {
            methodName = Constant.SELECT_LIST_BY_EXAMPLE;
        }
        TypeName sqlBuilderType = SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName);
        AnnotationSpec sqlBuilderAnnotation = MybatisFrameworkUtils.getSelectProviderAnnotationSpec(sqlBuilderType, methodName);
        TypeName returns;
        Iterable<ParameterSpec> pagingParameterSpecs = null;
        if (returnOne) {
            returns = EntityGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        } else {
            returns = EntityGeneratorUtil.listOfTableEntity(table, userSpecifyPackageName);
            pagingParameterSpecs = MyBatisMapperGeneratorUtil.getPagingParameterSpecs(true);
        }
        ParameterSpec whereClauseProvider = MyBatisMapperGeneratorUtil.getWhereClauseProviderParameterSpec(true);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addAnnotation(sqlBuilderAnnotation)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returns)
                .addParameter(whereClauseProvider);
        if (pagingParameterSpecs != null) {
            builder.addParameters(pagingParameterSpecs);
        }
        builder.addParameter(MyBatisMapperGeneratorUtil.getRetrieveColumnsParameterSpec(true));
        return builder.build();
    }

    /**
     * 生成<code>SELECT COUNT</code>方法,用于获取总数.
     *
     * @param table                  table
     * @param userSpecifyPackageName user specify package name
     * @return {@link MethodSpec}
     */
    private MethodSpec genSelectCountByExampleMethod(Table table, String userSpecifyPackageName) {
        String methodName = Constant.COUNT_BY_EXAMPLE;
        TypeName sqlBuilderType = SqlBuilderGeneratorUtil.getSqlBuilderTypeName(table, userSpecifyPackageName);
        AnnotationSpec sqlBuilderAnnotation = MybatisFrameworkUtils.getSelectProviderAnnotationSpec(sqlBuilderType, methodName);
        ParameterSpec whereClauseProvider = MyBatisMapperGeneratorUtil.getWhereClauseProviderParameterSpec(true);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addAnnotation(sqlBuilderAnnotation)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(Integer.TYPE)
                .addParameter(whereClauseProvider);
        return builder.build();
    }

    /**
     * 生成<code>SELECT COUNT</code>方法,用于获取总数.
     *
     * @param table                  table
     * @param userSpecifyPackageName user specify package name
     * @param whereClauseColumns     where clause columns
     * @return {@link MethodSpec}
     */
    private MethodSpec genSelectCountMethod(Table table, String userSpecifyPackageName, List<Column> whereClauseColumns) {
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
     *
     * @param table                  table
     * @param userSpecifyPackageName user specify package name
     * @param whereClauseColumns     where clause columns
     * @param primaryKey             是否主键
     * @return 方法定义
     */
    private MethodSpec genBatchSelectMethod(Table table, String userSpecifyPackageName, List<Column> whereClauseColumns, boolean primaryKey) {
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
