package com.wuda.code.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;

import java.util.List;
import java.util.Map;

/**
 * {@link ClassName}工具类.
 *
 * @author wuda
 */
public class TypeNameUtils {

    public static ClassName mapClassName() {
        return ClassName.get(Map.class);
    }

    public static ClassName listClassName() {
        return ClassName.get(List.class);
    }

    public static ParameterizedTypeName mapOf(Class keyClazz, Class valueClazz) {
        ClassName map = TypeNameUtils.mapClassName();
        return ParameterizedTypeName.get(map, ClassName.get(keyClazz), ClassName.get(valueClazz));
    }

    public static ParameterizedTypeName listOf(Class clazz) {
        ClassName listClassName = listClassName();
        return ParameterizedTypeName.get(listClassName, ClassName.get(clazz));
    }
}
