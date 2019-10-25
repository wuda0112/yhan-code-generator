package com.wuda.yhan.code.generator.lang.util;

import com.wuda.yhan.code.generator.lang.IsSetField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link IsSetField}工具类.
 *
 * @author wuda
 */
public class IsSetFieldUtils {

    /**
     * 获取实体中调用过setter方法的属性.
     * <strong>注意</strong>必须是使用了{@link IsSetField}特性的类才适用此方法,
     * 否则即使field调用了setter方法,也将永远返回空.
     *
     * @param obj
     *         使用了{@link IsSetField}特性的类.
     * @return 所有调用过setter方法的属性, null - 如果没有任何field调用过setter方法,或者没有使用{@link IsSetField}特性
     */
    public static Field[] setterCalledFields(Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] allFields = clazz.getDeclaredFields();
        if (allFields == null || allFields.length == 0) {
            return null;
        }
        List<Field> setterCalledFields = new ArrayList<>();
        for (Field field : allFields) {
            IsSetField isSetField = field.getDeclaredAnnotation(IsSetField.class);
            if (isSetField != null) {
                field.setAccessible(true);
                boolean isSet;
                try {
                    isSet = field.getBoolean(obj);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (isSet) {
                    String refFieldName = isSetField.referenceField();
                    try {
                        setterCalledFields.add(clazz.getDeclaredField(refFieldName));
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (setterCalledFields.isEmpty()) {
            return null;
        }
        return setterCalledFields.toArray(new Field[setterCalledFields.size()]);
    }
}
