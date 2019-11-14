package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Table;
import com.wuda.yhan.code.generator.lang.util.ColumnUtils;
import com.wuda.yhan.code.generator.lang.util.SqlProviderUtils;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成class,此class中包含了一个表的基本信息,比如表名,列名称等等.
 *
 * @author wuda
 */
public class TableMetaInfoGenerator {

    /**
     * 生成java class文件.
     *
     * @param table       表的基本信息
     * @param packageName 生成的类所属的包
     * @return java file
     */
    public JavaFile genJavaFile(Table table, String packageName) {
        String className = TableMetaInfoGeneratorUtil.toClassName(table.id().table());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className);
        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        classBuilder.addField(genSchemaField(table));
        classBuilder.addField(genTableNameField(table));
        classBuilder.addField(genSchemaDotTableField(table));
        classBuilder.addField(genPrimaryKeyField(table));
        FieldSpec autoIncrement = genAutoIncrementField(table);
        if (autoIncrement != null) {
            classBuilder.addField(autoIncrement);
        }
        Iterable<FieldSpec> fieldSpecs = genFields(table);
        if (fieldSpecs != null) {
            classBuilder.addFields(fieldSpecs);
        }
        String finalPackageName = PackageNameUtil.getEntityPackageName(packageName, table.id().schema());
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
            list.add(genField(table, column));
            list.add(genAsField(table, column));
            list.add(genAsFieldConcatTableName(table, column));
        }
        return list;
    }

    /**
     * TABLE_NAME field
     *
     * @param table table
     * @return field
     */
    private FieldSpec genTableNameField(Table table) {
        return genField(TableMetaInfoGeneratorUtil.getTableFieldName(), table.id().table());
    }

    /**
     * SCHEMA field
     *
     * @param table table
     * @return field
     */
    private FieldSpec genSchemaField(Table table) {
        return FieldSpec.builder(ClassName.get(String.class), TableMetaInfoGeneratorUtil.getSchemaFieldName(), Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", table.id().schema())
                .build();
    }

    /**
     * SCHEMA DOT TABLE field
     *
     * @param table table
     * @return field
     */
    private FieldSpec genSchemaDotTableField(Table table) {
        return genField(TableMetaInfoGeneratorUtil.getSchemaDotTableFieldName(), TableMetaInfoGeneratorUtil.getSchemaDotTableFieldValue(table));
    }

    /**
     * auto-increment field
     *
     * @param table table
     * @return field
     */
    private FieldSpec genAutoIncrementField(Table table) {
        Column column = ColumnUtils.getAutoIncrementColumn(table);
        if (column == null) {
            return null;
        }
        return FieldSpec.builder(ClassName.get(String.class), TableMetaInfoGeneratorUtil.getAutoIncrementColumn(), Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", column.name())
                .build();
    }

    /**
     * primary key field
     *
     * @param table table
     * @return field
     */
    private FieldSpec genPrimaryKeyField(Table table) {
        List<String> primaryKeyColumns = table.primaryKeyColumnNames();
        String doubleQuotedString = SqlProviderUtils.toDoubleQuotedString(primaryKeyColumns);
        return FieldSpec.builder(String[].class, TableMetaInfoGeneratorUtil.getPrimaryKeyFieldName(), Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("new String[]{$L}", doubleQuotedString)
                .build();
    }

    /**
     * 生成列对应的属性.
     *
     * @param table  表
     * @param column 列
     * @return 属性
     */
    private FieldSpec genField(Table table, Column column) {
        String columnName = column.name();
        String fieldName = TableMetaInfoGeneratorUtil.toFieldName(columnName);
        String fieldValue = TableMetaInfoGeneratorUtil.getSchemaDotTableFieldValue(table) + "." + columnName;
        return genField(fieldName, fieldValue);
    }

    /**
     * 生成列对应的别名属性.
     *
     * @param table  表
     * @param column 列
     * @return 属性
     */
    private FieldSpec genAsField(Table table, Column column) {
        String columnName = column.name();
        String fieldName = TableMetaInfoGeneratorUtil.toAsFieldName(columnName);
        String fieldValue = TableMetaInfoGeneratorUtil.getAsFieldValue(columnName);
        return genField(fieldName, fieldValue);
    }

    /**
     * 生成列对应的别名属性,和表名结合.
     *
     * @param table  table
     * @param column 列
     * @return 属性
     */
    private FieldSpec genAsFieldConcatTableName(Table table, Column column) {
        String columnName = column.name();
        String fieldName = TableMetaInfoGeneratorUtil.toAsFieldNameConcatTable(columnName);
        String fieldValue = TableMetaInfoGeneratorUtil.getAsFieldValueConcatTableName(table.id().table(), columnName);
        return genField(fieldName, fieldValue);
    }

    private FieldSpec genField(String fieldName, String fieldValue) {
        return FieldSpec.builder(ClassName.get(String.class), fieldName, Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", fieldValue)
                .build();
    }
}
