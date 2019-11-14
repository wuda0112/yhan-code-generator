package com.wuda.code.generator;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.wuda.yhan.code.generator.lang.util.JavaNamingUtils;

import javax.lang.model.element.Modifier;

/**
 * 方法定义 util.
 *
 * @author wuda
 * @see MethodSpec
 */
public class MethodSpecUtil {

    /**
     * 为给定的field生成getter方法.
     *
     * @param fieldSpec field
     * @return getter方法
     */
    public static MethodSpec genGetter(FieldSpec fieldSpec) {
        String methodName = JavaNamingUtils.genGetterMethodName(fieldSpec.name);
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(fieldSpec.type)
                .addStatement("return " + fieldSpec.name)
                .build();
    }

    /**
     * 生成setter方法.
     *
     * @param field 列对应的属性.
     * @return setter方法
     */
    public static MethodSpec genSetter(FieldSpec field) {
        String fieldName = field.name;
        String methodName = JavaNamingUtils.genSetterMethodName(fieldName);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(field.type, fieldName)
                .addStatement("this." + fieldName + "=" + fieldName);
        return methodBuilder.build();
    }
}
