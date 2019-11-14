package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import com.wuda.code.generator.MethodSpecUtil;
import com.wuda.yhan.code.generator.lang.TableEntity;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Table;

import javax.lang.model.element.Modifier;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成表对应的实体.
 *
 * @author wuda
 */
public class EntityGenerator {

    /**
     * 生成java class文件.
     *
     * @param table       表的基本信息
     * @param packageName 生成的类所属的包
     * @param jpa         是否生成jap相关的注解
     * @return java file
     */
    public JavaFile genJavaFile(Table table, String packageName, boolean jpa) {
        String className = EntityGeneratorUtil.toClassName(table.id().table());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className);
        classBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        classBuilder.addSuperinterface(TypeName.get(TableEntity.class));
        classBuilder.addSuperinterface(TypeName.get(Serializable.class));
        if (jpa) {
            classBuilder.addAnnotation(genTableAnnotation(table));
        }
        Iterable<FieldSpec> fieldSpecs = genFields(table.columns(), jpa);
        if (fieldSpecs != null) {
            classBuilder.addFields(fieldSpecs);
            classBuilder.addMethods(genGetterAndSetter(fieldSpecs));
        }
        String finalPackageName = PackageNameUtil.getEntityPackageName(packageName, table.id().schema());
        return JavaFile.builder(finalPackageName, classBuilder.build()).build();
    }

    /**
     * class级别的注解.
     *
     * @param table table
     * @return {@link javax.persistence.Table}注解
     */
    private AnnotationSpec genTableAnnotation(Table table) {
        return AnnotationSpec.builder(javax.persistence.Table.class)
                .addMember("schema", "$S", table.id().schema())
                .addMember("name", "$S", table.id().table())
                .build();
    }

    /**
     * field 上的注解.
     *
     * @param column 列
     * @return {@link javax.persistence.Column}注解
     */
    private AnnotationSpec genColumnAnnotation(Column column) {
        return AnnotationSpec.builder(javax.persistence.Column.class)
                .addMember("name", "$S", column.name())
                .addMember("length", "$L", column.length())
                .addMember("columnDefinition", "$S", column.typeExpression())
                .build();
    }

    /**
     * 为列生成对应的属性.
     *
     * @param columns 列
     * @param jpa     是否生成jpa相关的注解
     * @return iterator of fields
     */
    private Iterable<FieldSpec> genFields(List<Column> columns, boolean jpa) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        List<FieldSpec> list = new ArrayList<>(columns.size());
        for (Column column : columns) {
            list.add(genField(column, jpa));
        }
        return list;
    }

    /**
     * 生成列对应的属性.
     *
     * @param column 列
     * @param jpa    是否生成jpa相关注解
     * @return 属性
     */
    private FieldSpec genField(Column column, boolean jpa) {
        String columnName = column.name();
        Class<?> type = MysqlTypeUtil.mysqlTypeToJavaType(column.typeExpression());
        String fieldName = EntityGeneratorUtil.toFieldName(columnName);
        FieldSpec.Builder builder = FieldSpec.builder(type, fieldName, Modifier.PRIVATE);
        if (jpa) {
            AnnotationSpec annotationSpec = genColumnAnnotation(column);
            builder.addAnnotation(annotationSpec);
        }
        return builder.build();
    }

    /**
     * 为给定的field生成getter/setter.
     *
     * @param fieldSpecs field
     * @return getter/setter
     */
    private Iterable<MethodSpec> genGetterAndSetter(Iterable<FieldSpec> fieldSpecs) {
        List<MethodSpec> list = new ArrayList<>();
        for (FieldSpec fieldSpec : fieldSpecs) {
            list.add(MethodSpecUtil.genGetter(fieldSpec));
            list.add(MethodSpecUtil.genSetter(fieldSpec));
        }
        return list;
    }

}
