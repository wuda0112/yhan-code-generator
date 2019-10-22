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
     * @param table       表的基本信息
     * @param packageName 生成的类所属的包
     * @return java file
     */
    public JavaFile genJavaFile(Table table, String packageName) {
        String className = SqlTableGeneratorUtil.toClassName(table.id().table());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className);
        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        classBuilder.superclass(TypeName.get(SqlTable.class));
        classBuilder.addMethod(genConstructor(table));
        classBuilder.addField(genInstanceField(table, packageName));
        Iterable<FieldSpec> fieldSpecs = genFields(table);
        if (fieldSpecs != null) {
            classBuilder.addFields(fieldSpecs);
        }
        String finalPackageName = PackageNameUtil.getSqlTablePackageName(packageName, table.id().schema());
        return JavaFile.builder(finalPackageName, classBuilder.build()).build();
    }

    /**
     * 为列生成对应的属性.
     *
     * @param table table
     * @return iterator of fields
     */
    private Iterable<FieldSpec> genFields(Table table) {
        List<Column> columns = table.columns();
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        List<FieldSpec> list = new ArrayList<>(columns.size());
        for (Column column : columns) {
            list.add(genField(column));
        }
        return list;
    }

    /**
     * 生成列对应的属性.
     *
     * @param column 列
     * @return 属性
     */
    private FieldSpec genField(Column column) {
        String columnName = column.name();
        Class<?> type = MysqlTypeUtil.mysqlTypeToJavaType(column.typeExpression());
        String fieldName = TableMetaInfoGeneratorUtil.toFieldName(columnName);
        ClassName sqlColumnClassName = ClassName.get(SqlColumn.class);
        ParameterizedTypeName sqlColumnParameterizedTypeName = ParameterizedTypeName.get(sqlColumnClassName, ClassName.get(type));
        return FieldSpec.builder(sqlColumnParameterizedTypeName, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                .initializer("column($S)", columnName)
                .build();
    }

    /**
     * 生成构造函数.
     *
     * @param table table
     * @return 构造函数
     */
    private MethodSpec genConstructor(Table table) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super($S)", table.id().table())
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
