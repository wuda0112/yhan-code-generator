package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.yhan.code.generator.lang.SqlProviderUtils;
import com.wuda.yhan.code.generator.lang.TableEntity;
import com.wuda.yhan.code.generator.lang.TableEntityUtils;
import com.wuda.yhan.util.commons.IsSetFieldUtil;
import org.apache.ibatis.jdbc.SQL;

import javax.lang.model.element.Modifier;
import javax.persistence.Column;
import java.lang.reflect.Field;
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
     * @param table
     *         表的基本信息
     * @param packageName
     *         生成的类所属的包
     * @return java file
     */
    public JavaFile genJavaFile(Table table, String packageName) {
        String className = SqlBuilderGeneratorUtil.getClassName(table.getTableName());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className);
        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        classBuilder.addMethod(genInsertMethod(table, packageName));
        classBuilder.addMethod(genDeleteByPrimaryKeyMethod(table));
        classBuilder.addMethod(genSelectByPrimaryKeyMethod(table));
        classBuilder.addMethod(genUpdateByPrimaryKeyMethod(table, packageName));
        String finalPackageName = PackageNameUtil.getPackageName(packageName, table.getTableSchema());
        return JavaFile.builder(finalPackageName, classBuilder.build()).build();
    }

    /**
     * generate insert method.
     *
     * @param table
     *         表的基本信息
     * @param packageName
     *         生成的类所属的包
     * @return insert method
     */
    private MethodSpec genInsertMethod(Table table, String packageName) {
        String methodName = MyBatisMapperGeneratorUtil.getInsertMethodName();
        ParameterSpec parameterSpec = EntityGeneratorUtil.genEntityParameter(table, packageName);
        String schemaDotTable = PackageNameUtil.getSchemaDotTable(table);
        // todo
        ClassName string = ClassName.get("java.lang", "String");
        ClassName map = ClassName.get("java.util", "Map");
        TypeName mapOfString = ParameterizedTypeName.get(map, string, string);
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameter(parameterSpec)
                .addStatement("$T fieldToColumnMap = $T.fieldToColumn($L,$L)", mapOfString, TableEntityUtils.class, parameterSpec.name, true)
                .beginControlFlow("if (fieldToColumnMap == null || fieldToColumnMap.size() == 0)")
                .addStatement("throw new RuntimeException(\"没有属性调用过set方法!不生成insert sql语句!class name:\" + $L.getClass().getName())", parameterSpec.name)
                .endControlFlow()
                .addStatement("$T sql = new $T()", SQL.class, SQL.class)
                .addStatement("sql.INSERT_INTO($S)", schemaDotTable)
                .addStatement("$T.insertColumnsAndValues(sql,fieldToColumnMap)", SqlProviderUtils.class)
                .addStatement("return sql.toString()")
                .build();
    }

    /**
     * 为{@link #genInsertMethod(Table, String)}提供方法体模板.
     *
     * @param schemaDotTable
     *         schema.table
     * @param entity
     *         表对应的实体
     * @return sql
     */
    @SuppressWarnings("unused")
    private String insertMethodStatementTemplate(String schemaDotTable, TableEntity entity) {
        Map<String, String> fieldToColumnMap = TableEntityUtils.fieldToColumn(entity, true);
        if (fieldToColumnMap == null || fieldToColumnMap.size() == 0) {
            throw new RuntimeException("没有属性调用过set方法!不生成insert sql语句!class name:" + entity.getClass().getName());
        }
        SQL sql = new SQL();
        sql.INSERT_INTO(schemaDotTable);
        SqlProviderUtils.insertColumnsAndValues(sql, fieldToColumnMap);
        return sql.toString();
    }

    /**
     * generate delete method.
     *
     * @param table
     *         table
     * @return delete method
     */
    private MethodSpec genDeleteByPrimaryKeyMethod(Table table) {
        String methodName = MyBatisMapperGeneratorUtil.getDeleteByPrimaryKeyMethodName();
        Iterable<ParameterSpec> parameterSpecs = MyBatisMapperGeneratorUtil.genPrimaryKeyParameter(table, true);
        String schemaDotTable = PackageNameUtil.getSchemaDotTable(table);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.returns(String.class);
        builder.addParameters(parameterSpecs);
        builder.addStatement("$T sql = new $T()", SQL.class, SQL.class);
        builder.addStatement("sql.DELETE_FROM($S)", schemaDotTable);
        appendPrimaryKeyConditions(builder, table);
        builder.addStatement("return sql.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genDeleteByPrimaryKeyMethod(Table)}提供方法提的模板.
     *
     * @param schemaDotTable
     *         schema.table
     * @param primaryKeyColumns
     *         primary key
     * @return sql
     */
    @SuppressWarnings("unused")
    private String deleteByPrimaryKeyMethodStatementTemplate(String schemaDotTable, String[] primaryKeyColumns) {
        SQL sql = new SQL();
        sql.DELETE_FROM(schemaDotTable);
        appendPrimaryKeyConditions(sql, primaryKeyColumns);
        return sql.toString();
    }

    /**
     * generate update method.
     *
     * @param table
     *         table
     * @param userSpecifyPackageName
     *         package name
     * @return update method
     */
    private MethodSpec genUpdateByPrimaryKeyMethod(Table table, String userSpecifyPackageName) {
        String methodName = MyBatisMapperGeneratorUtil.getUpdateByPrimaryKeyMethodName();
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        ParameterSpec entityParameter = EntityGeneratorUtil.genEntityParameter(table, userSpecifyPackageName);
        String schemaDotTable = PackageNameUtil.getSchemaDotTable(table);
        ClassName columnClass = ClassName.get(Column.class);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.returns(String.class);
        builder.addParameter(entityParameter);
        builder.addStatement("$T[] setterCalledFields = $T.setterCalledFields($L)", Field.class, IsSetFieldUtil.class, entityParameter.name);
        builder.beginControlFlow("if (setterCalledFields == null || setterCalledFields.length == 0)");
        builder.addStatement("throw new RuntimeException(\"没有属性被调用过set方法!不生成update sql语句!class name:\" + $L.getClass().getName())", entityParameter.name);
        builder.endControlFlow();
        builder.addStatement("$T sql = new $T()", SQL.class, SQL.class);
        builder.addStatement("sql.UPDATE($S)", schemaDotTable);
        builder.beginControlFlow("for ($T field : setterCalledFields)", Field.class);
        builder.addStatement("$T columnAnnotation = field.getAnnotation($T.class)", Column.class, columnClass);
        builder.addStatement("$T columnName = columnAnnotation.name()", String.class);
        builder.addStatement("$T fieldName = field.getName()", String.class);
        builder.addStatement("$T sb = new $T(columnName.length() + fieldName.length() + 3)", StringBuilder.class, StringBuilder.class);
        builder.addStatement("sql.SET(sb.append(columnName).append(\"=\").append(\"#{\").append(fieldName).append(\"}\").toString())");
        builder.endControlFlow();
        appendPrimaryKeyConditions(builder, table);
        builder.addStatement("return sql.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genUpdateByPrimaryKeyMethod(Table, String)}提供方法体模板.
     *
     * @param schemaDotTable
     *         schema.table
     * @param primaryKeyColumns
     *         primary key
     * @param entity
     *         表对应的实体
     * @return sql
     */
    @SuppressWarnings("unused")
    private String updateByPrimaryKeyMethodStatementTemplate(String schemaDotTable, String[] primaryKeyColumns, Object entity) {
        Field[] setterCalledFields = IsSetFieldUtil.setterCalledFields(entity);
        if (setterCalledFields == null || setterCalledFields.length == 0) {
            throw new RuntimeException("没有属性被调用过set方法!不生成update sql语句!class name:" + entity.getClass().getName());
        }

        SQL sql = new SQL();
        sql.UPDATE(schemaDotTable);
        for (Field field : setterCalledFields) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = columnAnnotation.name();
            String fieldName = field.getName();
            StringBuilder sb = new StringBuilder(columnName.length() + fieldName.length() + 3);

            sql.SET(sb.append(columnName).append("=").append("#{").append(fieldName).append("}").toString());
        }
        appendPrimaryKeyConditions(sql, primaryKeyColumns);
        return sql.toString();
    }

    /**
     * generate select method.
     *
     * @param table
     *         table
     * @return select method
     */
    private MethodSpec genSelectByPrimaryKeyMethod(Table table) {
        String methodName = MyBatisMapperGeneratorUtil.getSelectByPrimaryKeyMethodName();
        String schemaDotTable = PackageNameUtil.getSchemaDotTable(table);
        Iterable<ParameterSpec> primaryKeyParameter = MyBatisMapperGeneratorUtil.genPrimaryKeyParameter(table, true);
        ParameterSpec retrieveColumnParameter = MyBatisMapperGeneratorUtil.genRetrieveColumnsParam(true);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.returns(String.class);
        builder.addParameters(primaryKeyParameter);
        builder.addParameter(retrieveColumnParameter);
        builder.beginControlFlow("if ($L == null || $L.length == 0)", retrieveColumnParameter.name, retrieveColumnParameter.name);
        builder.addStatement("throw new RuntimeException(\"必须指定需要返回的列!\")");
        builder.endControlFlow();
        builder.addStatement("$T sql = new $T()", SQL.class, SQL.class);
        builder.addStatement("sql.SELECT($L).FROM($S)", retrieveColumnParameter.name, schemaDotTable);
        appendPrimaryKeyConditions(builder, table);
        builder.addStatement("return sql.toString()");
        return builder.build();
    }

    /**
     * 为{@link #genSelectByPrimaryKeyMethod(Table)}提供方法体模板.
     *
     * @param schemaDotTable
     *         schema.table
     * @param primaryKeyColumns
     *         primary key
     * @param columns
     *         需要返回的列
     * @return sql
     */
    @SuppressWarnings("unused")
    private String selectByPrimaryKeyMethodStatementTemplate(String schemaDotTable, String[] primaryKeyColumns, String... columns) {
        if (columns == null || columns.length == 0) {
            throw new RuntimeException("必须指定需要返回的列!");
        }
        SQL sql = new SQL();
        sql.SELECT(columns).FROM(schemaDotTable);
        appendPrimaryKeyConditions(sql, primaryKeyColumns);
        return sql.toString();
    }

    /**
     * 为sql添加主键查询条件.
     *
     * @param sql
     *         SQL
     * @param primaryKeyColumns
     *         主键中的列
     */
    private void appendPrimaryKeyConditions(SQL sql, String[] primaryKeyColumns) {
        String fieldName;
        for (String columnName : primaryKeyColumns) {
            fieldName = EntityGeneratorUtil.genFieldName(columnName);
            sql.WHERE(columnName + "=#{" + fieldName + "}");
        }
    }

    /**
     * 为方法追加主键的查询条件.
     *
     * @param builder
     *         MethodSpec.Builder
     * @param table
     *         table contains primary key
     */
    private void appendPrimaryKeyConditions(MethodSpec.Builder builder, Table table) {
        String[] primaryKeyColumns = table.getPrimaryKeyColumns();
        for (String columnName : primaryKeyColumns) {
            String fieldName = EntityGeneratorUtil.genFieldName(columnName);
            builder.addStatement("sql.WHERE(\"$L=#{$L}\")", columnName, fieldName);
        }
    }
}
