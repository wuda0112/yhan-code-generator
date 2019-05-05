package com.wuda.code.generator.db.mysql;

import com.squareup.javapoet.AnnotationSpec;
import org.apache.ibatis.annotations.Param;

/**
 * Mybatis framework utils.
 *
 * @author wuda
 */
class MybatisFrameworkUtils {

    /**
     * 生成{@link Param}注解.
     *
     * @param parameterName parameter name
     * @return {@link Param}注解
     */
    static AnnotationSpec getMybatisParamAnnotationSpec(String parameterName) {
        return AnnotationSpec.builder(Param.class)
                .addMember("value", "$S", parameterName)
                .build();
    }

}
