package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Table;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 为每个表生成{@link org.mybatis.dynamic.sql.SqlTable}的子类，用于支持动态sql.
 *
 * @author wuda
 */
public class SqlTableGenerator {

    /**
     * 生成java class文件.
     *
     * @param table                  表的基本信息
     * @param userSpecifyPackageName 用户指定的包名称
     * @return java file
     */
    public JavaFile genJavaFile(Table table, String userSpecifyPackageName) {
        String className = SqlTableGeneratorUtil.toClassName(table.id().table());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className);
        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        classBuilder.superclass(TypeName.get(SqlTable.class));
        classBuilder.addMethod(genConstructor(table, userSpecifyPackageName));
        classBuilder.addField(genInstanceField(table, userSpecifyPackageName));
        Iterable<FieldSpec> fieldSpecs = genFields(table, userSpecifyPackageName);
        if (fieldSpecs != null) {
            classBuilder.addFields(fieldSpecs);
        }
        String finalPackageName = PackageNameUtil.getSqlTablePackageName(userSpecifyPackageName, table.id().schema());
        return JavaFile.builder(finalPackageName, classBuilder.build()).build();
    }

    /**
     * 为列生成对应的属性.
     *
     * @param table                  table
     * @param userSpecifyPackageName 包名
     * @return iterator of fields
     */
    private Iterable<FieldSpec> genFields(Table table, String userSpecifyPackageName) {
        List<Column> columns = table.columns();
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        List<FieldSpec> list = new ArrayList<>(columns.size());
        for (Column column : columns) {
            list.add(genField(table, column, userSpecifyPackageName));
        }
        return list;
    }

    /**
     * 生成列对应的属性.
     *
     * @param table                  table
     * @param column                 列
     * @param userSpecifyPackageName 包名
     * @return 属性
     */
    private FieldSpec genField(Table table, Column column, String userSpecifyPackageName) {
        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);

        String columnName = column.name();
        Class<?> type = MysqlTypeUtil.mysqlTypeToJavaType(column.typeExpression());
        String fieldName = TableMetaInfoGeneratorUtil.toFieldName(columnName);
        ClassName sqlColumnClassName = ClassName.get(SqlColumn.class);
        ParameterizedTypeName sqlColumnParameterizedTypeName = ParameterizedTypeName.get(sqlColumnClassName, ClassName.get(type));
        return FieldSpec.builder(sqlColumnParameterizedTypeName, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                .initializer("column($L.$L)", tableMetaInfo, fieldName)
                .build();
    }

    /**
     * 生成构造函数.
     *
     * @param table                  table
     * @param userSpecifyPackageName 用户指定的包名
     * @return 构造函数
     */
    private MethodSpec genConstructor(Table table, String userSpecifyPackageName) {
        TypeName tableMetaInfo = TableMetaInfoGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        String tableName = TableMetaInfoGeneratorUtil.getTableFieldName();
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super($L.$L)", tableMetaInfo, tableName)
                .build();
    }

    /**
     * 生成静态实例.
     *
     * @param table                  table
     * @param userSpecifyPackageName 包名
     * @return 实例的属性
     */
    private FieldSpec genInstanceField(Table table, String userSpecifyPackageName) {
        String fieldName = Constant.SQL_TABLE_INSTANCE;
        TypeName typeName = SqlTableGeneratorUtil.getTypeName(table, userSpecifyPackageName);
        return FieldSpec.builder(typeName, fieldName, Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("new $T()", typeName)
                .build();
    }
}
