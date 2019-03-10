package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.*;
import org.apache.ibatis.annotations.*;

import javax.lang.model.element.Modifier;
import java.util.TreeSet;

/**
 * 生成表对应的mybatis mapper接口.
 *
 * @author wuda
 */
public class MyBatisMapperGenerator {

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
        String className = MyBatisMapperGeneratorUtil.genClassName(table.getTableName());
        TypeSpec.Builder classBuilder = TypeSpec.interfaceBuilder(className);
        classBuilder.addAnnotation(genMapperAnnotation());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addMethod(genInsertMethod(table, packageName));
        classBuilder.addMethod(genBatchInsertMethod(table, packageName));
        classBuilder.addMethod(genDeleteByPrimaryKeyMethod(table, packageName));
        classBuilder.addMethod(genUpdateMethod(table, packageName));
        classBuilder.addMethod(genSelectByPrimaryKeyMethod(table, packageName));
        String finalPackageName = PackageNameUtil.getPackageName(packageName, table.getTableSchema());
        return JavaFile.builder(finalPackageName, classBuilder.build()).build();
    }

    /**
     * {@link Mapper}注解
     *
     * @return {@link Mapper}注解
     */
    private AnnotationSpec genMapperAnnotation() {
        return AnnotationSpec.builder(Mapper.class).build();
    }

    /**
     * 生成insert方法.
     *
     * @param table
     *         table
     * @param packageName
     *         package name
     * @return insert method
     */
    private MethodSpec genInsertMethod(Table table, String packageName) {
        String methodName = MyBatisMapperGeneratorUtil.getInsertMethodName();
        ParameterSpec parameterSpec = EntityGeneratorUtil.genEntityParameter(table, packageName);
        return MethodSpec.methodBuilder(methodName)
                .addAnnotation(insertMethodAnnotation(table, packageName))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameter(parameterSpec)
                .build();
    }

    /**
     * {@link #genInsertMethod(Table, String)}方法上的{@link InsertProvider}注解.
     *
     * @param table
     *         table
     * @param packageName
     *         package name
     * @return {@link InsertProvider}
     */
    private AnnotationSpec insertMethodAnnotation(Table table, String packageName) {
        return AnnotationSpec.builder(InsertProvider.class)
                .addMember("type", "$T.class", SqlBuilderGeneratorUtil.genSqlBuilderClassName(table, packageName))
                .addMember("method", "$S", MyBatisMapperGeneratorUtil.getInsertMethodName())
                .build();
    }

    /**
     * 生成批量insert方法.
     *
     * @param table
     *         table
     * @param packageName
     *         package name
     * @return batch insert method
     */
    private MethodSpec genBatchInsertMethod(Table table, String packageName) {
        String methodName = MyBatisMapperGeneratorUtil.getBatchInsertMethodName();
        ParameterSpec parameterSpec = EntityGeneratorUtil.genEntityListParameter(table, packageName);
        return MethodSpec.methodBuilder(methodName)
                .addAnnotation(batchInsertMethodAnnotation(table))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameter(parameterSpec)
                .build();
    }

    /**
     * {@link #genBatchInsertMethod(Table, String)}方法上的{@link Insert}注解.
     *
     * @param table
     *         table
     * @return {@link Insert}
     */
    private AnnotationSpec batchInsertMethodAnnotation(Table table) {
        return AnnotationSpec.builder(Insert.class)
                .addMember("value", "$S", batchInsertScript(table))
                .build();
    }

    /**
     * batch insert sql语句.也就是方法上{@link Insert}注解的值.
     *
     * @param table
     *         数据库表
     * @return 批量插入方法的sql
     */
    private String batchInsertScript(Table table) {
        String schemaDotTable = PackageNameUtil.getSchemaDotTable(table);
        StringBuilder builder = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        builder.append("<script>");
        builder.append(newLine);
        builder.append("INSERT INTO ").append(schemaDotTable);

        TreeSet<Table.ColumnMetaInfo> columnMetaInfoSet = table.getColumns();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        String columnName;
        for (Table.ColumnMetaInfo columnMetaInfo : columnMetaInfoSet) {
            columnName = columnMetaInfo.getColumnName();
            columns.append(columnName).append(",");
            values.append("#{entity.").append(EntityGeneratorUtil.genFieldName(columnName)).append("},");
        }

        builder.append("(").append(columns.substring(0, columns.length() - 1)).append(")");
        String item = "entity";
        String collection = MyBatisMapperGeneratorUtil.getBatchInsertParamName();
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
     * 生成delete by primary key方法.
     *
     * @param table
     *         table
     * @param packageName
     *         package name
     * @return method
     */
    private MethodSpec genDeleteByPrimaryKeyMethod(Table table, String packageName) {
        return MethodSpec.methodBuilder(MyBatisMapperGeneratorUtil.getDeleteByPrimaryKeyMethodName())
                .addAnnotation(deleteByPrimaryKeyMethodAnnotation(table, packageName))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameters(MyBatisMapperGeneratorUtil.genPrimaryKeyParameter(table, true))
                .build();
    }

    /**
     * 为{@link #genDeleteByPrimaryKeyMethod(Table, String)}生成{@link DeleteProvider}注解.
     *
     * @param table
     *         table
     * @param packageName
     *         package name
     * @return {@link DeleteProvider}
     */
    private AnnotationSpec deleteByPrimaryKeyMethodAnnotation(Table table, String packageName) {
        return AnnotationSpec.builder(DeleteProvider.class)
                .addMember("type", "$T.class", SqlBuilderGeneratorUtil.genSqlBuilderClassName(table, packageName))
                .addMember("method", "$S", MyBatisMapperGeneratorUtil.getDeleteByPrimaryKeyMethodName())
                .build();
    }

    /**
     * 生成update方法.
     *
     * @param table
     *         table
     * @param packageName
     *         package name
     * @return update method
     */
    private MethodSpec genUpdateMethod(Table table, String packageName) {
        String methodName = MyBatisMapperGeneratorUtil.getUpdateByPrimaryKeyMethodName();
        ParameterSpec parameterSpec = EntityGeneratorUtil.genEntityParameter(table, packageName);
        return MethodSpec.methodBuilder(methodName)
                .addAnnotation(updateMethodAnnotation(table, packageName))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.INT)
                .addParameter(parameterSpec)
                .build();
    }

    /**
     * {@link #genUpdateMethod(Table, String)}方法上的{@link UpdateProvider}注解.
     *
     * @param table
     *         table
     * @param packageName
     *         package name
     * @return {@link UpdateProvider}
     */
    private AnnotationSpec updateMethodAnnotation(Table table, String packageName) {
        return AnnotationSpec.builder(UpdateProvider.class)
                .addMember("type", "$T.class", SqlBuilderGeneratorUtil.genSqlBuilderClassName(table, packageName))
                .addMember("method", "$S", MyBatisMapperGeneratorUtil.getUpdateByPrimaryKeyMethodName())
                .build();
    }

    /**
     * 生成select by primary key方法.
     *
     * @param table
     *         table
     * @param packageName
     *         package name
     * @return method
     */
    private MethodSpec genSelectByPrimaryKeyMethod(Table table, String packageName) {
        return MethodSpec.methodBuilder(MyBatisMapperGeneratorUtil.getSelectByPrimaryKeyMethodName())
                .addAnnotation(selectByPrimaryKeyMethodAnnotation(table, packageName))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(EntityGeneratorUtil.genEntityClassName(table, packageName))
                .addParameters(MyBatisMapperGeneratorUtil.genPrimaryKeyParameter(table, true))
                .addParameter(MyBatisMapperGeneratorUtil.genRetrieveColumnsParam(true))
                .build();
    }

    /**
     * 为{@link #genSelectByPrimaryKeyMethod(Table, String)}生成{@link SelectProvider}注解.
     *
     * @param table
     *         table
     * @param packageName
     *         package name
     * @return {@link SelectProvider}
     */
    private AnnotationSpec selectByPrimaryKeyMethodAnnotation(Table table, String packageName) {
        return AnnotationSpec.builder(SelectProvider.class)
                .addMember("type", "$T.class", SqlBuilderGeneratorUtil.genSqlBuilderClassName(table, packageName))
                .addMember("method", "$S", MyBatisMapperGeneratorUtil.getSelectByPrimaryKeyMethodName())
                .build();
    }
}
