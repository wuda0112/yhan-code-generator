package com.wuda.yhan.code.generator.lang.util;

import com.wuda.yhan.code.generator.lang.PojoFieldInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * bean utils.
 *
 * @author wuda
 */
public class BeanUtils {

    /**
     * 注解包含策略.
     *
     * @author wuda
     */
    public enum AnnotationContainsPolicy {
        /**
         * 必须包含所有给定的注解.
         */
        CONTAINS_ALL,
        /**
         * 至少包含其中一个注解.
         */
        AT_LEAST_ONE,
        /**
         * 一个都不能包含.
         */
        CONTAINS_ZERO;
    }

    /**
     * 获取POJO中属性的信息.
     *
     * @param clazz                    POJO clazz
     * @param mustHasGetter            返回的属性是否必须包含get方法
     * @param mustHasSetter            返回的属性是否必须包含set方法
     * @param annotationClassSet       属性的注解
     * @param annotationContainsPolicy 对于给定的注解,该属性的包含策略
     * @return 属性信息, null-如果该POJO中不包含任何属性,或者不满足给定条件
     */
    public static <T extends Annotation> List<PojoFieldInfo> getFieldInfoList(Class<?> clazz, boolean mustHasGetter,
                                                                              boolean mustHasSetter, Set<Class<T>> annotationClassSet,
                                                                              AnnotationContainsPolicy annotationContainsPolicy) {
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null || fields.length == 0) {
            return null;
        }
        List<PojoFieldInfo> list = new ArrayList<>(fields.length);
        for (Field field : fields) {
            List<Annotation> annotations = null;
            if (annotationClassSet != null) {
                annotations = getAnnotations(field, annotationClassSet, annotationContainsPolicy);
                if (annotations == null) {
                    continue;
                }
            }
            String fieldName = field.getName();
            Method getter = getter(clazz, fieldName);
            if (getter == null && mustHasGetter) {
                continue;
            }
            Method setter = setter(clazz, fieldName);
            if (setter == null && mustHasSetter) {
                continue;
            }
            PojoFieldInfo pojoFieldInfo = new PojoFieldInfo();
            pojoFieldInfo.setField(field);
            pojoFieldInfo.setGetter(getter);
            pojoFieldInfo.setSetter(setter);
            pojoFieldInfo.setAnnotations(annotations);
            list.add(pojoFieldInfo);
        }
        return list;
    }

    /**
     * 返回属性的get方法.
     *
     * @param clazz     clazz
     * @param fieldName 属性名称
     * @return get方法, 如果没有该属性, 或者该属性没有get方法, 则返回null.
     */
    public static Method getter(Class<?> clazz, String fieldName) {
        String getterName = JavaNamingUtils.genGetterMethodName(fieldName);
        Method getter;
        try {
            getter = clazz.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            getter = null;
        }
        return getter;
    }

    /**
     * 获取java bean中属性的值.
     *
     * @param bean      java bean
     * @param fieldName 属性
     * @return 属性值
     */
    public static Object getValue(Object bean, String fieldName) {
        Method getter = getter(bean.getClass(), fieldName);
        if (getter == null) {
            throw new RuntimeException(bean.getClass().getCanonicalName() + ",field=" + fieldName + ",没有getter");
        }
        try {
            return getter.invoke(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回属性的set方法.
     *
     * @param clazz     clazz
     * @param fieldName 属性名称
     * @return set方法, 如果没有该属性, 或者该属性没有set方法, 则返回null.
     */
    public static Method setter(Class<?> clazz, String fieldName) {
        Method setter;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            String setterName = JavaNamingUtils.genSetterMethodName(fieldName);
            setter = clazz.getMethod(setterName, field.getType());
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            setter = null;
        }
        return setter;
    }

    public static <T extends Annotation> List<Annotation> getAnnotations(Field field, Set<Class<T>> annotationClassSet,
                                                                         AnnotationContainsPolicy annotationContainsPolicy) {
        List<Annotation> list = new ArrayList<Annotation>();
        for (Class<T> annotationClass : annotationClassSet) {
            Annotation[] array = getAnnotation(field, annotationClass);
            if (annotationContainsPolicy == AnnotationContainsPolicy.CONTAINS_ALL && (array == null || array.length == 0)) {
                return null;
            } else if (annotationContainsPolicy == AnnotationContainsPolicy.CONTAINS_ZERO
                    && (array != null && array.length > 0)) {
                return null;
            }
            Collections.addAll(list, array);
        }
        if (annotationContainsPolicy == AnnotationContainsPolicy.AT_LEAST_ONE && list.isEmpty()) {
            return null;
        }
        return list;
    }

    public static <T extends Annotation> Annotation[] getAnnotation(Field field, Class<T> annotationClass) {
        return field.getAnnotationsByType(annotationClass);
    }

}
