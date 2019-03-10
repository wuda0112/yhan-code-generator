package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.code.generator.MethodSpecUtil;
import com.wuda.yhan.code.generator.lang.TableEntity;
import com.wuda.yhan.util.commons.IsSetField;

import javax.lang.model.element.Modifier;
import javax.persistence.Column;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * 生成表对应的实体.
 *
 * @author wuda
 */
public class EntityGenerator {

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
        String className = EntityGeneratorUtil.genClassName(table.getTableName());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className);
        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        classBuilder.addSuperinterface(TypeName.get(TableEntity.class));
        classBuilder.addSuperinterface(TypeName.get(Serializable.class));
        classBuilder.addAnnotation(genTableAnnotation(table));
        Iterable<FieldSpec> fieldSpecs = genFields(table.getColumns());
        if (fieldSpecs != null) {
            classBuilder.addFields(fieldSpecs);
            classBuilder.addMethods(genGetterAndSetter(fieldSpecs));
        }
        String finalPackageName = PackageNameUtil.getPackageName(packageName, table.getTableSchema());
        return JavaFile.builder(finalPackageName, classBuilder.build()).build();
    }

    /**
     * class级别的注解.
     *
     * @param table
     *         table
     * @return {@link javax.persistence.Table}注解
     */
    private AnnotationSpec genTableAnnotation(Table table) {
        return AnnotationSpec.builder(javax.persistence.Table.class)
                .addMember("schema", "$S", table.getTableSchema())
                .addMember("name", "$S", table.getTableName())
                .build();
    }

    /**
     * field 上的注解.
     *
     * @param columnMetaInfo
     *         列的元数据
     * @return {@link Column}注解
     */
    private AnnotationSpec genColumnAnnotation(Table.ColumnMetaInfo columnMetaInfo) {
        return AnnotationSpec.builder(Column.class)
                .addMember("name", "$S", columnMetaInfo.getColumnName())
                .addMember("length", "$L", columnMetaInfo.getLength())
                .addMember("columnDefinition", "$S", columnMetaInfo.getColumnDefinition())
                .build();
    }

    /**
     * 为列生成对应的属性.
     *
     * @param columns
     *         列
     * @return iterator of fields
     */
    private Iterable<FieldSpec> genFields(TreeSet<Table.ColumnMetaInfo> columns) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        List<FieldSpec> list = new ArrayList<>(columns.size());
        for (Table.ColumnMetaInfo columnMetaInfo : columns) {
            list.add(genField(columnMetaInfo));
            list.add(genIsSetField(columnMetaInfo));
        }
        return list::iterator;
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
        Class<?> type = MysqlTypeUtil.mysqlTypeToJavaType(columnMetaInfo.getDataType());
        String fieldName = EntityGeneratorUtil.genFieldName(columnName);
        AnnotationSpec annotationSpec = genColumnAnnotation(columnMetaInfo);
        return FieldSpec.builder(type, fieldName, Modifier.PRIVATE)
                .addAnnotation(annotationSpec)
                .build();
    }

    /**
     * 如果列的名称是: product_name,此方法将生成<pre>private boolean productNameIsSet;</pre>
     * 属性.
     *
     * @param columnMetaInfo
     *         列
     * @return field
     */
    private FieldSpec genIsSetField(Table.ColumnMetaInfo columnMetaInfo) {
        String columnName = columnMetaInfo.getColumnName();
        String fieldName = EntityGeneratorUtil.genIsSetFieldName(columnName);
        return FieldSpec.builder(TypeName.BOOLEAN, fieldName, Modifier.PRIVATE)
                .addAnnotation(genIsSetFieldAnnotation(columnName))
                .build();
    }

    /**
     * 生成{@link IsSetField}注解.
     *
     * @param columnName
     *         列名
     * @return {@link IsSetField}
     */
    private AnnotationSpec genIsSetFieldAnnotation(String columnName) {
        String referenceFieldName = EntityGeneratorUtil.genFieldName(columnName);
        return AnnotationSpec.builder(IsSetField.class)
                .addMember("referenceField", "$S", referenceFieldName)
                .build();
    }

    /**
     * 为给定的field生成getter/setter.
     *
     * @param fieldSpecs
     *         field
     * @return getter/setter
     */
    private Iterable<MethodSpec> genGetterAndSetter(Iterable<FieldSpec> fieldSpecs) {
        List<MethodSpec> list = new ArrayList<>();
        for (FieldSpec fieldSpec : fieldSpecs) {
            list.add(MethodSpecUtil.genGetter(fieldSpec));
            if (!hasIsSetFieldAnnotation(fieldSpec.annotations)) {
                list.add(MethodSpecUtil.genSetter(fieldSpec, true));
            }
        }
        return list::iterator;
    }

    private boolean hasIsSetFieldAnnotation(List<AnnotationSpec> annotationSpecs) {
        if (annotationSpecs == null || annotationSpecs.isEmpty()) {
            return false;
        }
        TypeName isSetFieldAnnotation = TypeName.get(IsSetField.class);
        for (AnnotationSpec annotationSpec : annotationSpecs) {
            if (annotationSpec.type.equals(isSetFieldAnnotation)) {
                return true;
            }
        }
        return false;
    }

}
