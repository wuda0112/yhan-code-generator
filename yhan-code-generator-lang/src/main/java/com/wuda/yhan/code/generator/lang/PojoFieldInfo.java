package com.wuda.yhan.code.generator.lang;

import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 描述POJO中属性的信息.
 *
 * @author wuda
 */
@Getter
@Setter
public class PojoFieldInfo {

    /**
     * 属性.
     */
    private Field field;
    /**
     * 属性的get方法.
     */
    private Method getter;
    /**
     * 属性的set方法.
     */
    private Method setter;
    /**
     * 注解.
     */
    private List<Annotation> annotations;

}
