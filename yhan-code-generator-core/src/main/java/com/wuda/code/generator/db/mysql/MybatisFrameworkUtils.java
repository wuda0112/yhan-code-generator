package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;
import org.apache.ibatis.annotations.*;

/**
 * Mybatis framework utils.
 *
 * @author wuda
 */
class MybatisFrameworkUtils {

    /**
     * {@link Mapper}注解
     *
     * @return {@link Mapper}注解
     */
    static AnnotationSpec genMapperAnnotation() {
        return AnnotationSpec.builder(Mapper.class).build();
    }

    /**
     * 生成{@link Param}注解.
     *
     * @param parameterName parameter name
     * @return {@link Param}注解
     */
    static AnnotationSpec getParamAnnotationSpec(String parameterName) {
        return AnnotationSpec.builder(Param.class)
                .addMember("value", "$S", parameterName)
                .build();
    }

    /**
     * {@link InsertProvider}注解.
     *
     * @param sqlBuilderTypeName 提供sql语句的类
     * @param methodName         提供sql语句的方法
     * @return 注解
     */
    static AnnotationSpec getInsertProviderAnnotationSpec(TypeName sqlBuilderTypeName, String methodName) {
        return AnnotationSpec.builder(InsertProvider.class)
                .addMember("type", "$T.class", sqlBuilderTypeName)
                .addMember("method", "$S", methodName)
                .build();
    }

    /**
     * {@link DeleteProvider}注解.
     *
     * @param sqlBuilderTypeName 提供sql语句的类
     * @param methodName         提供sql语句的方法
     * @return 注解
     */
    static AnnotationSpec getDeleteProviderAnnotationSpec(TypeName sqlBuilderTypeName, String methodName) {
        return AnnotationSpec.builder(DeleteProvider.class)
                .addMember("type", "$T.class", sqlBuilderTypeName)
                .addMember("method", "$S", methodName)
                .build();
    }

    /**
     * {@link UpdateProvider}注解.
     *
     * @param sqlBuilderTypeName 提供sql语句的类
     * @param methodName         提供sql语句的方法
     * @return 注解
     */
    static AnnotationSpec getUpdateProviderAnnotationSpec(TypeName sqlBuilderTypeName, String methodName) {
        return AnnotationSpec.builder(UpdateProvider.class)
                .addMember("type", "$T.class", sqlBuilderTypeName)
                .addMember("method", "$S", methodName)
                .build();
    }

    /**
     * {@link SelectProvider}注解.
     *
     * @param sqlBuilderTypeName 提供sql语句的类
     * @param methodName         提供sql语句的方法
     * @return 注解
     */
    static AnnotationSpec getSelectProviderAnnotationSpec(TypeName sqlBuilderTypeName, String methodName) {
        return AnnotationSpec.builder(SelectProvider.class)
                .addMember("type", "$T.class", sqlBuilderTypeName)
                .addMember("method", "$S", methodName)
                .build();
    }

    /**
     * {@link Insert}注解.
     *
     * @param value value
     * @return 注解
     */
    static AnnotationSpec getInsertAnnotationSpec(String value) {
        return AnnotationSpec.builder(Insert.class)
                .addMember("value", "$S", value)
                .build();
    }

    /**
     * {@link SelectKey}注解.
     *
     * @param keyProperty {@link SelectKey#keyProperty()}
     * @param before      {@link SelectKey#before()}
     * @param resultType  {@link SelectKey#resultType()}
     * @return 注解
     */
    static AnnotationSpec getSelectKeyAnnotationSpec(String keyProperty, boolean before, Class<?> resultType) {
        return AnnotationSpec.builder(SelectKey.class)
                .addMember("statement", "$S", "SELECT LAST_INSERT_ID()")
                .addMember("keyProperty", "$S", keyProperty)
                .addMember("before", "$T", before)
                .addMember("resultType", "$T", resultType)
                .build();
    }

    /**
     * {@link Options}注解中,用于获取<code>AUTO_INCREMENT</code>值的相关内容.
     *
     * @param useGeneratedKeys {@link Options#useGeneratedKeys()}
     * @param keyProperty      {@link Options#keyProperty()}.
     *                         <a href="http://www.mybatis.org/mybatis-3/sqlmap-xml.html">mybatis</a>
     * @return 注解
     */
    static AnnotationSpec getKeyGenerateAnnotationSpec(boolean useGeneratedKeys, String keyProperty) {
        return AnnotationSpec.builder(Options.class)
                .addMember("keyProperty", "$S", keyProperty)
                .addMember("useGeneratedKeys", "$L", useGeneratedKeys)
                .build();
    }

}
