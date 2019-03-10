package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * 生成class,此class中包含了一个表的基本信息,比如表名,列名称等等.
 *
 * @author wuda
 */
public class TableMetaInfoGenerator {

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
        String className = TableMetaInfoGeneratorUtil.genClassName(table.getTableName());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className);
        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        classBuilder.addField(genSchemaField(table));
        classBuilder.addField(genTableNameField(table));
        Iterable<FieldSpec> fieldSpecs = genFields(table);
        if (fieldSpecs != null) {
            classBuilder.addFields(fieldSpecs);
        }
        String finalPackageName = PackageNameUtil.getPackageName(packageName, table.getTableSchema());
        return JavaFile.builder(finalPackageName, classBuilder.build()).build();
    }

    /**
     * 为列生成对应的属性.
     *
     * @param table
     *         table
     * @return iterator of fields
     */
    private Iterable<FieldSpec> genFields(Table table) {
        TreeSet<Table.ColumnMetaInfo> columns = table.getColumns();
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        List<FieldSpec> list = new ArrayList<>(columns.size());
        for (Table.ColumnMetaInfo columnMetaInfo : columns) {
            list.add(genField(columnMetaInfo));
            list.add(genAsField(columnMetaInfo));
            list.add(genAsFieldConcatTableName(table, columnMetaInfo));
        }
        return list::iterator;
    }

    /**
     * TABLE_NAME field
     *
     * @param table
     *         table
     * @return field
     */
    private FieldSpec genTableNameField(Table table) {
        return FieldSpec.builder(ClassName.get(String.class), TableMetaInfoGeneratorUtil.getTableNameFieldName(), Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", table.getTableName())
                .build();
    }

    /**
     * SCHEMA field
     *
     * @param table
     *         table
     * @return field
     */
    private FieldSpec genSchemaField(Table table) {
        return FieldSpec.builder(ClassName.get(String.class), TableMetaInfoGeneratorUtil.getSchemaFieldName(), Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", table.getTableSchema())
                .build();
    }

    /**
     * 生成列对应的属性.
     *
     * @param columnMetaInfo
     *         列
     * @return 属性
     */
    private FieldSpec genField(Table.ColumnMetaInfo columnMetaInfo) {
        String columnName = columnMetaInfo.getColumnName();
        String fieldName = TableMetaInfoGeneratorUtil.genFieldName(columnName);
        return FieldSpec.builder(ClassName.get(String.class), fieldName, Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", columnName)
                .build();
    }

    /**
     * 生成列对应的别名属性.
     *
     * @param columnMetaInfo
     *         列
     * @return 属性
     */
    private FieldSpec genAsField(Table.ColumnMetaInfo columnMetaInfo) {
        String columnName = columnMetaInfo.getColumnName();
        String fieldName = TableMetaInfoGeneratorUtil.genAsFieldName(columnName);
        String fieldValue = TableMetaInfoGeneratorUtil.getAsFieldValue(columnName);
        return FieldSpec.builder(ClassName.get(String.class), fieldName, Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", fieldValue)
                .build();
    }

    /**
     * 生成列对应的别名属性,和表名结合.
     *
     * @param table
     *         table
     * @param columnMetaInfo
     *         列
     * @return 属性
     */
    private FieldSpec genAsFieldConcatTableName(Table table, Table.ColumnMetaInfo columnMetaInfo) {
        String columnName = columnMetaInfo.getColumnName();
        String fieldName = TableMetaInfoGeneratorUtil.genAsFieldNameConcatTable(columnName);
        String fieldValue = TableMetaInfoGeneratorUtil.getAsFieldValueConcatTableName(table.getTableName(), columnName);
        return FieldSpec.builder(ClassName.get(String.class), fieldName, Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", fieldValue)
                .build();
    }
}
