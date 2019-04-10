package com.wuda.code.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * {@link ClassName}工具类.
 *
 * @author wuda
 */
public class TypeNameUtils {

    public static TypeName stringTypeName() {
        return ClassName.get("java.lang", "String");
    }

    public static ClassName mapClassName() {
        return ClassName.get("java.util", "Map");
    }

    public static ClassName listClassName() {
        return ClassName.get("java.util", "List");
    }

    public static ParameterizedTypeName mapOfString() {
        TypeName string = TypeNameUtils.stringTypeName();
        ClassName map = TypeNameUtils.mapClassName();
        return ParameterizedTypeName.get(map, string, string);
    }
}
