package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.code.generator.CodeGenerateException;
import com.wuda.code.generator.TypeNameUtils;
import com.wuda.yhan.code.generator.lang.*;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Index;
import com.wuda.yhan.code.generator.lang.relational.Table;
import org.apache.ibatis.jdbc.SQL;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 生成每个表的sql builder类.
 *
 * @author wuda
 */
public class SqlBuilderGenerator {

    /**
     * 生成java class文件.
     *
     * @param table       表的基本信息
     * @param packageName 生成的类所属的包
     * @return java file
     */
    public JavaFile genJavaFile(Table table, String packageName) {
        String className = SqlBuilderGeneratorUtil.toClassName(table.id().table());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className);
        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        classBuilder.addMethod(genInsertMethod(table, packageName));
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
        String finalPackageName = PackageNameUtil.getMapperPackageName(packageName, table.id().schema());
        return JavaFile.builder(finalPackageName, classBuilder.build()).build();
    }

    /**
     * generate insert method.
     *
     * @param table                  表的基本信息
     * @param userSpecifyPackageName 用户指定的包
     * @return insert method
     */
    private MethodSpec genInsertMethod(Table table, String userSpecifyPackageName) {
        String methodName = Constant.MAPPER_INSERT;
        ParameterSpec parameterSpec = EntityGeneratorUtil.getEntityParameter(table, userSpecifyPackageName, false);

        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String schemaDotTable = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName();

        ParameterizedTypeName mapOfString = TypeNameUtils.mapOf(String.class, String.class);

        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameter(parameterSpec)
                .addStatement("$T.validate($L)", SqlProviderUtils.class, parameterSpec.name)
                .addStatement("$T setterCalledFieldToColumnMap = $T.fieldToColumn($L,$L)", mapOfString, TableEntityUtils.class, parameterSpec.name, true)
                .addStatement("$T.setterCalledFieldValidate($L, setterCalledFieldToColumnMap)", SqlProviderUtils.class, parameterSpec.name)
                .addStatement("$T sql = new $T()", SQL.class, SQL.class)
                .addStatement("sql.INSERT_INTO($T.$L)", tableMetaInfo, schemaDotTable)
                .addStatement("$T.insertColumnsAndValues(sql,setterCalledFieldToColumnMap)", SqlProviderUtils.class)
                .addStatement("return sql.toString()")
                .build();
    }

    /**
     * 为{@link #genInsertMethod(Table, String)}提供方法体模板.
     *
     * @param schemaDotTable schema.table
     * @param entity         表对应的实体
     * @return sql
     */
    @SuppressWarnings("unused")
    private String insertMethodStatementTemplate(String schemaDotTable, TableEntity entity) {
        SqlProviderUtils.validate(entity);
        Map<String, String> setterCalledFieldToColumnMap = TableEntityUtils.fieldToColumn(entity, true);
        SqlProviderUtils.setterCalledFieldValidate(entity, setterCalledFieldToColumnMap);
        SQL sql = new SQL();
        sql.INSERT_INTO(schemaDotTable);
        SqlProviderUtils.insertColumnsAndValues(sql, setterCalledFieldToColumnMap);
        return sql.toString();
    }


    /**
     * generate insert method.
     *
     * @param table                  表的基本信息
     * @param userSpecifyPackageName 用户指定的包
     * @return insert method
     */
    private MethodSpec genBatchInsertUseGeneratedKeysMethod(Table table, String userSpecifyPackageName) {
        if (ColumnUtils.getAutoIncrementColumn(table) == null) {
            return null;
        }
        String methodName = Constant.MAPPER_BATCH_INSERT_USE_GENERATED_KEYS;
        ParameterSpec parameterSpec = EntityGeneratorUtil.getEntityListParameter(table, userSpecifyPackageName);

        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String schemaDotTable = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName();
        String autoIncrementColumn = TableMetaInfoGeneratorUtil.getAutoIncrementColumn();

        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameter(parameterSpec)
                .addStatement("$T sql = new $T()", SQL.class, SQL.class)
                .addStatement("sql.INSERT_INTO($T.$L)", tableMetaInfo, schemaDotTable)
                .addStatement("$T.batchInsertUseGeneratedKeysColumnsAndValues(sql,$L,$S,$T.$L)", SqlProviderUtils.class, parameterSpec.name, parameterSpec.name, tableMetaInfo, autoIncrementColumn)
                .addStatement("return sql.toString()")
                .build();
    }

    /**
     * 为{@link #genBatchInsertUseGeneratedKeysMethod(Table, String)}提供方法体模板.
     *
     * @param schemaDotTable      schema.table
     * @param list                表对应的实体
     * @param collectionName      集合名称,类似Mybatis foreach中的collection
     * @param autoIncrementColumn auto-increment column
     * @return sql
     */
    @SuppressWarnings("unused")
    private String batchInsertUseGeneratedKeysMethodStatementTemplate(String schemaDotTable, List<TableEntity> list, String collectionName, String autoIncrementColumn) {
        SQL sql = new SQL();
        sql.INSERT_INTO(schemaDotTable);
        SqlProviderUtils.batchInsertUseGeneratedKeysColumnsAndValues(sql, list, collectionName, autoIncrementColumn);
        return sql.toString();
    }

    /**
     * generate delete method.
     *
     * @param table                  table
     * @param userSpecifyPackageName 用户指定的包名
     * @param whereClauseColumns     删除条件的列
     * @param primaryKey             <i>whereClauseColumns</i>是否主键
     * @return delete method
     */
    private MethodSpec genDeleteMethod(Table table, String userSpecifyPackageName, List<Column> whereClauseColumns, boolean primaryKey) {
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getDeleteMethodName(columnNames, primaryKey);
        Iterable<ParameterSpec> parameterSpecs = MyBatisMapperGeneratorUtil.getParameterSpecs(whereClauseColumns, true);

        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String schemaDotTable = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.returns(String.class);
        builder.addParameters(parameterSpecs);
        builder.addStatement("$T sql = new $T()", SQL.class, SQL.class);
        builder.addStatement("sql.DELETE_FROM($T.$L)", tableMetaInfo, schemaDotTable);
        if (primaryKey) {
            String primaryKeyFieldName = TableMetaInfoGeneratorUtil.getPrimaryKeyFieldName();
            builder.addStatement("$T.whereConditions(sql, $T.$L)", SqlProviderUtils.class, tableMetaInfo, primaryKeyFieldName);
        } else {
            String whereClauseColumnQuotingString = SqlProviderUtils.toDoubleQuotedString(columnNames);
            builder.addStatement("$T.whereConditions(sql, $L)", SqlProviderUtils.class, whereClauseColumnQuotingString);
        }
        builder.addStatement("return sql.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genDeleteMethod(Table, String, List, boolean)}提供方法提的模板.
     *
     * @param schemaDotTable     schema.table
     * @param whereClauseColumns 删除使用的条件
     * @return sql
     */
    @SuppressWarnings("unused")
    private String deleteByPrimaryKeyMethodStatementTemplate(String schemaDotTable, String... whereClauseColumns) {
        SQL sql = new SQL();
        sql.DELETE_FROM(schemaDotTable);
        SqlProviderUtils.whereConditions(sql, whereClauseColumns);
        return sql.toString();
    }

    /**
     * generate update method.
     * 没有限制更新的条件必须是主键,可以根据任何字段生成更新方法.
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
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        Iterable<ParameterSpec> conditionsParameterSpec = MyBatisMapperGeneratorUtil.getParameterSpecs(whereClauseColumns, true);
        ParameterSpec updateParameterSpec = EntityGeneratorUtil.getEntityParameter(table, userSpecifyPackageName, true);

        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String schemaDotTable = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName();
        String primaryKeyFieldName = TableMetaInfoGeneratorUtil.getPrimaryKeyFieldName();

        ParameterizedTypeName mapOfString = TypeNameUtils.mapOf(String.class, String.class);

        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.returns(String.class);
        builder.addParameters(conditionsParameterSpec);
        builder.addParameter(updateParameterSpec);
        builder.addStatement("$T.validate($L)", SqlProviderUtils.class, updateParameterSpec.name);
        builder.addStatement("$T setterCalledFieldToColumnMap = $T.fieldToColumn($L,$L)", mapOfString, TableEntityUtils.class, updateParameterSpec.name, true);
        builder.addStatement("$T.setterCalledFieldValidate($L,setterCalledFieldToColumnMap)", SqlProviderUtils.class, updateParameterSpec.name);
        builder.addStatement("$T sql = new $T()", SQL.class, SQL.class);
        builder.addStatement("sql.UPDATE($T.$L)", tableMetaInfo, schemaDotTable);
        if (primaryKey) {
            builder.addStatement("$T.updateSetColumnsAndValues(sql,setterCalledFieldToColumnMap,$S,$T.$L)", SqlProviderUtils.class, updateParameterSpec.name, tableMetaInfo, primaryKeyFieldName);
            builder.addStatement("$T.whereConditions(sql, $T.$L)", SqlProviderUtils.class, tableMetaInfo, primaryKeyFieldName);
        } else {
            List<String> list = new ArrayList<>(table.primaryKeyColumnNames().size() + columnNames.size());
            list.addAll(table.primaryKeyColumnNames());
            list.addAll(columnNames);
            String exclusiveColumnQuotingString = SqlProviderUtils.toDoubleQuotedString(list);
            builder.addStatement("$T.updateSetColumnsAndValues(sql,setterCalledFieldToColumnMap,$S,$L)", SqlProviderUtils.class, updateParameterSpec.name, exclusiveColumnQuotingString);
            String whereClauseColumnQuotingString = SqlProviderUtils.toDoubleQuotedString(columnNames);
            builder.addStatement("$T.whereConditions(sql, $L)", SqlProviderUtils.class, whereClauseColumnQuotingString);
        }
        builder.addStatement("return sql.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genUpdateMethod(Table, String, List, boolean)}提供方法体模板.
     *
     * @param schemaDotTable     schema.table
     * @param entity             表对应的实体
     * @param parameterName      参数名称,Mybatis的参数名称
     * @param whereClauseColumns 更新条件的列
     * @return sql
     */
    @SuppressWarnings("unused")
    private String updateMethodStatementTemplate(String schemaDotTable, TableEntity entity, String parameterName, String... whereClauseColumns) {
        SqlProviderUtils.validate(entity);
        Map<String, String> setterCalledFieldToColumnMap = TableEntityUtils.fieldToColumn(entity, true);
        SqlProviderUtils.setterCalledFieldValidate(entity, setterCalledFieldToColumnMap);
        SQL sql = new SQL();
        sql.UPDATE(schemaDotTable);
        SqlProviderUtils.updateSetColumnsAndValues(sql, setterCalledFieldToColumnMap, parameterName, whereClauseColumns);
        SqlProviderUtils.whereConditions(sql, whereClauseColumns);
        return sql.toString();
    }

    /**
     * 给定Table和Where条件字段,生成此表上相应的查询方法.
     * 这里没有强制要求给定的列必须有索引,
     * 所以可以生成此表上任何列的查询方法,
     * 不过最好还是在有索引的列上生成查询方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName 用户指定的包名称
     * @param whereClauseColumns     sql查询语句中where条件的列
     * @param primaryKey             给定的这些列是否组成主键
     * @param uniqueIndex            给定的这些列是否唯一索引
     * @param forUpdate              SELECT ... FOR UPDATE
     * @return 查询方法
     */
    private MethodSpec genSelectMethod(Table table, String userSpecifyPackageName, List<Column> whereClauseColumns, boolean primaryKey, boolean uniqueIndex, boolean forUpdate) {
        if ((!primaryKey && !uniqueIndex) && forUpdate) {
            throw new CodeGenerateException("select...for update语法的查询必须在主键或者唯一索引字段上才生成");
        }
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getSelectMethodName(columnNames, primaryKey, forUpdate);

        Iterable<ParameterSpec> whereClauseParameterSpecs = MyBatisMapperGeneratorUtil.getParameterSpecs(whereClauseColumns, true);
        ParameterSpec retrieveColumnParameter = MyBatisMapperGeneratorUtil.getRetrieveColumnsParameterSpec(true);

        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String schemaDotTable = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameters(whereClauseParameterSpecs);
        boolean paging = false;
        if (!primaryKey && !uniqueIndex) {
            builder.addParameters(MyBatisMapperGeneratorUtil.getPagingParameterSpecs(true));
            paging = true;
        }
        builder.addParameter(retrieveColumnParameter)
                .addStatement("$T.selectColumnsValidate($L)", SqlProviderUtils.class, retrieveColumnParameter.name)
                .addStatement("$T sql = new $T()", SQL.class, SQL.class)
                .addStatement("sql.SELECT($L)", retrieveColumnParameter.name)
                .addStatement("sql.FROM($T.$L)", tableMetaInfo, schemaDotTable);
        if (primaryKey) {
            builder.addStatement("$T.whereConditions(sql, $T.$L)", SqlProviderUtils.class, tableMetaInfo, TableMetaInfoGeneratorUtil.getPrimaryKeyFieldName());
        } else {
            String whereClauseColumnQuotingString = SqlProviderUtils.toDoubleQuotedString(columnNames);
            builder.addStatement("$T.whereConditions(sql, $L)", SqlProviderUtils.class, whereClauseColumnQuotingString);
        }
        builder.addStatement("$T builder = new $T()", StringBuilder.class, StringBuilder.class)
                .addStatement("sql.usingAppender(builder)");
        if (forUpdate) {
            builder.addStatement("$T.appendForUpdate(builder)", SqlProviderUtils.class);
        }
        if (paging) {
            builder.addStatement("$T.appendPaging(builder)", SqlProviderUtils.class);
        }
        builder.addStatement("return builder.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genSelectMethod(Table, String, List, boolean, boolean, boolean)}提供方法体模板.
     *
     * @param schemaDotTable     schema.table
     * @param whereClauseColumns where条件中的columns
     * @param forUpdate          SELECT ... FOR UPDATE
     * @param paging             是否分页
     * @param offset             分页的offset
     * @param rowCount           分页的row count
     * @param columns            需要返回的列
     * @return sql
     */
    @SuppressWarnings("unused")
    private String selectMethodStatementTemplate(String schemaDotTable, String[] whereClauseColumns, boolean forUpdate, boolean paging, int offset, int rowCount, String... columns) {
        SqlProviderUtils.selectColumnsValidate(columns);
        SQL sql = new SQL();
        sql.SELECT(columns);
        sql.FROM(schemaDotTable);
        SqlProviderUtils.whereConditions(sql, whereClauseColumns);
        StringBuilder builder = new StringBuilder();
        sql.usingAppender(builder);
        if (forUpdate) {
            SqlProviderUtils.appendForUpdate(builder);
        }
        if (paging) {
            SqlProviderUtils.appendPaging(builder);
        }
        return builder.toString();
    }

    /**
     * 给定Table和Where条件字段,生成此表上相应的<code>SELECT COUNT</code>方法.
     * 这里没有强制要求给定的列必须有索引,
     * 所以可以生成此表上任何列的查询方法,
     * 不过最好还是在有索引的列上生成查询方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName 用户指定的包名称
     * @param whereClauseColumns     sql查询语句中where条件的列
     * @return 查询方法
     */
    private MethodSpec genSelectCountMethod(Table table, String userSpecifyPackageName, List<Column> whereClauseColumns) {
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getSelectCountMethodName(columnNames);

        Iterable<ParameterSpec> whereClauseParameterSpecs = MyBatisMapperGeneratorUtil.getParameterSpecs(whereClauseColumns, true);

        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String schemaDotTable = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameters(whereClauseParameterSpecs);
        builder.addStatement("$T sql = new $T()", SQL.class, SQL.class)
                .addStatement("sql.SELECT($T.COUNT_STATEMENT)", Constant.class)
                .addStatement("sql.FROM($T.$L)", tableMetaInfo, schemaDotTable);
        String whereClauseColumnQuotingString = SqlProviderUtils.toDoubleQuotedString(columnNames);
        builder.addStatement("$T.whereConditions(sql, $L)", SqlProviderUtils.class, whereClauseColumnQuotingString);
        builder.addStatement("$T builder = new $T()", StringBuilder.class, StringBuilder.class)
                .addStatement("sql.usingAppender(builder)");
        builder.addStatement("return builder.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genSelectCountMethod(Table, String, List)}提供方法体模板.
     *
     * @param schemaDotTable     schema.table
     * @param whereClauseColumns where条件中的columns
     * @return sql
     */
    @SuppressWarnings("unused")
    private String selectCountMethodStatementTemplate(String schemaDotTable, String[] whereClauseColumns) {
        SQL sql = new SQL();
        sql.SELECT(Constant.COUNT_STATEMENT);
        sql.FROM(schemaDotTable);
        SqlProviderUtils.whereConditions(sql, whereClauseColumns);
        StringBuilder builder = new StringBuilder();
        sql.usingAppender(builder);
        return builder.toString();
    }

    /**
     * 给定Table和Where条件字段,生成此表上相应的批量查询方法,类似于Mybatis foreach语法.
     * 这里没有强制要求给定的列必须有索引,
     * 所以可以生成此表上任何列的查询方法,
     * 不过最好还是在有索引的列上生成查询方法.
     *
     * @param table                  table
     * @param userSpecifyPackageName 用户指定的包名称
     * @param whereClauseColumns     sql查询语句中where条件的列
     * @param primaryKey             给定的这些列是否组成主键
     * @return 查询方法
     */
    private MethodSpec genBatchSelectMethod(Table table, String userSpecifyPackageName, List<Column> whereClauseColumns, boolean primaryKey) {
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getBatchSelectMethodName(columnNames, primaryKey);

        ParameterSpec parameterSpec = MyBatisMapperGeneratorUtil.getBatchSelectParameterSpec(whereClauseColumns, true, table, userSpecifyPackageName);
        ParameterSpec retrieveColumnParameter = MyBatisMapperGeneratorUtil.getRetrieveColumnsParameterSpec(true);

        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String schemaDotTable = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName();

        String collectionName = MyBatisMapperGeneratorUtil.getListParamName();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameter(parameterSpec)
                .addParameter(retrieveColumnParameter);
        builder.addStatement("$T.selectColumnsValidate($L)", SqlProviderUtils.class, retrieveColumnParameter.name)
                .addStatement("$T sql = new $T()", SQL.class, SQL.class)
                .addStatement("sql.SELECT($L)", retrieveColumnParameter.name)
                .addStatement("sql.FROM($T.$L)", tableMetaInfo, schemaDotTable);
        if (primaryKey) {
            builder.addStatement("$T.whereConditionsForeach(sql,$S,$L.size(), $T.$L)", SqlProviderUtils.class, collectionName, parameterSpec.name, tableMetaInfo, TableMetaInfoGeneratorUtil.getPrimaryKeyFieldName());
        } else {
            String whereClauseColumnQuotingString = SqlProviderUtils.toDoubleQuotedString(columnNames);
            builder.addStatement("$T.whereConditionsForeach(sql,$S,$L.size(), $L)", SqlProviderUtils.class, collectionName, parameterSpec.name, whereClauseColumnQuotingString);
        }
        builder.addStatement("$T builder = new $T()", StringBuilder.class, StringBuilder.class)
                .addStatement("sql.usingAppender(builder)");
        builder.addStatement("return builder.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genBatchSelectMethod(Table, String, List, boolean)}提供方法体模板.
     *
     * @param schemaDotTable     schema.table
     * @param collectionName     集合名称,类似Mybatis foreach中的collection
     * @param collectionSize     集合的大小
     * @param whereClauseColumns where条件中的columns
     * @param retrieveColumns    需要返回的列
     * @return sql
     */
    @SuppressWarnings("unused")
    private String batchSelectMethodStatementTemplate(String schemaDotTable, String collectionName, int collectionSize, String[] whereClauseColumns, String... retrieveColumns) {
        SqlProviderUtils.selectColumnsValidate(retrieveColumns);
        SQL sql = new SQL();
        sql.SELECT(retrieveColumns);
        sql.FROM(schemaDotTable);
        SqlProviderUtils.whereConditionsForeach(sql, collectionName, collectionSize, whereClauseColumns);
        StringBuilder builder = new StringBuilder();
        sql.usingAppender(builder);
        return builder.toString();
    }
}
