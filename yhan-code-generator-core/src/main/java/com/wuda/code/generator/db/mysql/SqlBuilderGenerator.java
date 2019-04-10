package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.code.generator.TypeNameUtils;
import com.wuda.yhan.code.generator.lang.SqlProviderUtils;
import com.wuda.yhan.code.generator.lang.TableEntity;
import com.wuda.yhan.code.generator.lang.TableEntityUtils;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.ColumnUtils;
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
        classBuilder.addMethod(genDeleteByPrimaryKeyMethod(table, packageName));
        classBuilder.addMethod(genUpdateByPrimaryKeyMethod(table, packageName));
        classBuilder.addMethod(genSelectByPrimaryKeyMethod(table, packageName));
        Iterable<MethodSpec> selectByIndexMethods = genSelectByIndexMethodSpec(table, packageName);
        if (selectByIndexMethods != null) {
            classBuilder.addMethods(selectByIndexMethods);
        }
        String finalPackageName = PackageNameUtil.getPackageName(packageName, table.id().schema());
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
        String methodName = MyBatisMapperGeneratorUtil.getInsertMethodName();
        ParameterSpec parameterSpec = EntityGeneratorUtil.getEntityParameter(table, userSpecifyPackageName);

        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String schemaDotTable = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName();

        ParameterizedTypeName mapOfString = TypeNameUtils.mapOfString();

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
     * generate delete method.
     *
     * @param table                  table
     * @param userSpecifyPackageName 用户指定的包名
     * @return delete method
     */
    private MethodSpec genDeleteByPrimaryKeyMethod(Table table, String userSpecifyPackageName) {
        String methodName = MyBatisMapperGeneratorUtil.getDeleteByPrimaryKeyMethodName();
        Iterable<ParameterSpec> parameterSpecs = MyBatisMapperGeneratorUtil.getPrimaryKeyParameterSpec(table, true);

        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String schemaDotTable = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName();
        String primaryKey = TableMetaInfoGeneratorUtil.getPrimaryKeyFieldName();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.returns(String.class);
        builder.addParameters(parameterSpecs);
        builder.addStatement("$T sql = new $T()", SQL.class, SQL.class);
        builder.addStatement("sql.DELETE_FROM($T.$L)", tableMetaInfo, schemaDotTable);
        builder.addStatement("$T.whereConditions(sql, $T.$L)", SqlProviderUtils.class, tableMetaInfo, primaryKey);
        builder.addStatement("return sql.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genDeleteByPrimaryKeyMethod(Table, String)}提供方法提的模板.
     *
     * @param schemaDotTable    schema.table
     * @param primaryKeyColumns primary key columns
     * @return sql
     */
    @SuppressWarnings("unused")
    private String deleteByPrimaryKeyMethodStatementTemplate(String schemaDotTable, String... primaryKeyColumns) {
        SQL sql = new SQL();
        sql.DELETE_FROM(schemaDotTable);
        SqlProviderUtils.whereConditions(sql, primaryKeyColumns);
        return sql.toString();
    }

    /**
     * generate update method.
     *
     * @param table                  table
     * @param userSpecifyPackageName package name
     * @return update method
     */
    private MethodSpec genUpdateByPrimaryKeyMethod(Table table, String userSpecifyPackageName) {
        String methodName = MyBatisMapperGeneratorUtil.getUpdateByPrimaryKeyMethodName();
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        ParameterSpec entityParameter = EntityGeneratorUtil.getEntityParameter(table, userSpecifyPackageName);

        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String schemaDotTable = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName();
        String primaryKey = TableMetaInfoGeneratorUtil.getPrimaryKeyFieldName();

        ParameterizedTypeName mapOfString = TypeNameUtils.mapOfString();

        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.returns(String.class);
        builder.addParameter(entityParameter);
        builder.addStatement("$T.validate($L)", SqlProviderUtils.class, entityParameter.name);
        builder.addStatement("$T setterCalledFieldToColumnMap = $T.fieldToColumn($L,$L)", mapOfString, TableEntityUtils.class, entityParameter.name, true);
        builder.addStatement("$T.setterCalledFieldValidate($L,setterCalledFieldToColumnMap)", SqlProviderUtils.class, entityParameter.name);
        builder.addStatement("$T sql = new $T()", SQL.class, SQL.class);
        builder.addStatement("sql.UPDATE($T.$L)", tableMetaInfo, schemaDotTable);
        builder.addStatement("$T.exclusiveUpdateColumns(setterCalledFieldToColumnMap, $T.$L)", SqlProviderUtils.class, tableMetaInfo, primaryKey);
        builder.addStatement("$T.updateSetColumnsAndValues(sql,setterCalledFieldToColumnMap)", SqlProviderUtils.class);
        builder.addStatement("$T.whereConditions(sql, $T.$L)", SqlProviderUtils.class, tableMetaInfo, primaryKey);
        builder.addStatement("return sql.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genUpdateByPrimaryKeyMethod(Table, String)}提供方法体模板.
     *
     * @param schemaDotTable    schema.table
     * @param entity            表对应的实体
     * @param primaryKeyColumns primary key columns
     * @return sql
     */
    @SuppressWarnings("unused")
    private String updateByPrimaryKeyMethodStatementTemplate(String schemaDotTable, TableEntity entity, String... primaryKeyColumns) {
        SqlProviderUtils.validate(entity);
        Map<String, String> setterCalledFieldToColumnMap = TableEntityUtils.fieldToColumn(entity, true);
        SqlProviderUtils.setterCalledFieldValidate(entity, setterCalledFieldToColumnMap);
        SQL sql = new SQL();
        sql.UPDATE(schemaDotTable);
        SqlProviderUtils.exclusiveUpdateColumns(setterCalledFieldToColumnMap, primaryKeyColumns);
        SqlProviderUtils.updateSetColumnsAndValues(sql, setterCalledFieldToColumnMap);
        SqlProviderUtils.whereConditions(sql, primaryKeyColumns);
        return sql.toString();
    }

    /**
     * generate select by primary key method.
     *
     * @param table                  table
     * @param userSpecifyPackageName 用户指定的包名
     * @return select by primary method
     */
    private MethodSpec genSelectByPrimaryKeyMethod(Table table, String userSpecifyPackageName) {
        List<Column> primaryKeyColumns = table.primaryKeyColumns();
        return genSelectMethod(table, primaryKeyColumns, true, false, userSpecifyPackageName);
    }

    /**
     * 给定Table和Where条件字段,生成此表上相应的查询方法.
     * 这里没有强制要求给定的列必须有索引,
     * 所以可以生成此表上任何列的查询方法,
     * 不过最好还是在有索引的列上生成查询方法.
     *
     * @param table                  table
     * @param whereClauseColumns     sql查询语句中where条件的列
     * @param primaryKey             给定的这些列是否组成主键
     * @param uniqueIndex            给定的这些列是否唯一索引
     * @param userSpecifyPackageName 用户指定的包名称
     * @return 查询方法
     */
    private MethodSpec genSelectMethod(Table table, List<Column> whereClauseColumns, boolean primaryKey, boolean uniqueIndex, String userSpecifyPackageName) {
        List<String> columnNames = ColumnUtils.columnNames(whereClauseColumns);
        String methodName = MyBatisMapperGeneratorUtil.getSelectMethodName(columnNames, primaryKey);

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
        if (paging) {
            builder.addStatement("$T.appendPaging(builder)", SqlProviderUtils.class);
        }
        builder.addStatement("return builder.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genSelectMethod(Table, List, boolean, boolean, String)}提供方法体模板.
     *
     * @param schemaDotTable     schema.table
     * @param whereClauseColumns where条件中的columns
     * @param paging             是否分页
     * @param offset             分页的offset
     * @param rowCount           分页的row count
     * @param columns            需要返回的列
     * @return sql
     */
    @SuppressWarnings("unused")
    private String selectMethodStatementTemplate(String schemaDotTable, String[] whereClauseColumns, boolean paging, int offset, int rowCount, String... columns) {
        SqlProviderUtils.selectColumnsValidate(columns);
        SQL sql = new SQL();
        sql.SELECT(columns);
        sql.FROM(schemaDotTable);
        SqlProviderUtils.whereConditions(sql, whereClauseColumns);
        StringBuilder builder = new StringBuilder();
        sql.usingAppender(builder);
        if (paging) {
            SqlProviderUtils.appendPaging(builder);
        }
        return builder.toString();
    }

    /**
     * 根据表中定义的索引(不包含主键)生成对应的满足索引的查询方法.
     * 每个索引对应一个查询的方法.
     * <p>
     * 方法名的定义如下
     * <ul>
     * <li>如果是主键,方法名是{@link MyBatisMapperGeneratorUtil#getSelectByPrimaryKeyMethodName}</li>
     * <li>非主键索引,把索引中的字段用"And"连接起来,再加上<i>selectBy</i>前缀.
     * </ul>
     * 方法输入参数的定义如下
     * <ul>
     * <li><i>WHERE</i>条件中的字段,一定是组成索引的列</li>
     * <li>返回的列</li>
     * <li>分页参数,如果是唯一索引,则没有分页参数,其他都有分页相关的参数</li>
     * </ul>
     * 方法返回值的定义如下
     * <ul>
     * <li>如果是唯一索引(包含主键)则返回单个实体,其他都返回实体的集合,list of entity.</li>
     * </ul>
     * </p>
     * 举例,一个非主键普通索引由<i>store_id</i>和<i>is_deleted</i>两个字段组成,
     * 那么生成的查询方法定义伪代码是
     * <pre>
     * public List<TableEntity> selectByStoreIdAndIsDeleted(Long storeId,boolean isDeleted,String[] retrieveColumns,int offset,int limit);
     * </pre>
     *
     * @param table                  数据库中表的定义
     * @param userSpecifyPackageName 包名称
     * @return 所有索引对应的查询方法
     */
    private Iterable<MethodSpec> genSelectByIndexMethodSpec(Table table, String userSpecifyPackageName) {
        List<Index> indices = table.getIndices();
        if (indices == null || indices.isEmpty()) {
            return null;
        }
        List<MethodSpec> methods = new ArrayList<>(indices.size());
        for (Index index : indices) {
            List<Column> indexColumns = ColumnUtils.indexColumns(table, index);
            MethodSpec methodSpec = genSelectMethod(table, indexColumns, false, index.getType() == Index.Type.UNIQUE, userSpecifyPackageName);
            methods.add(methodSpec);
        }
        return methods;
    }
}
