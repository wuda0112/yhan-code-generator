package com.wuda.yhan.code.generator.lang;

import com.wuda.yhan.util.commons.BeanUtils;
import com.wuda.yhan.util.commons.BeanUtils.AnnotationContainsPolicy;
import com.wuda.yhan.util.commons.IsSetFieldUtil;
import com.wuda.yhan.util.commons.PojoFieldInfo;
import com.wuda.yhan.util.commons.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * {@link TableEntity}的工具类.
 *
 * @author wuda
 */
public class TableEntityUtils {

    /**
     * logger.
     */
    private static Logger logger = LoggerFactory.getLogger(TableEntityUtils.class);

    /**
     * 所有entity的class是否同一个类型.
     *
     * @param entities entities
     * @return true-如果都是同一个class类型
     */
    public static boolean sameType(List<TableEntity> entities) {
        if (entities == null || entities.size() == 0) {
            return true;
        }
        Class first = entities.get(0).getClass();
        for (int i = 1; i < entities.size(); i++) {
            if (!entities.get(i).getClass().equals(first)) {
                return false;
            }
        }
        return true;
    }

    /**
     * key是属性名称,value该属性对应的数据库表的列名.
     *
     * @param entity                表对应的实体
     * @param onlySetterCalledField 是否值获取调用过setter方法的属性,
     *                              具体查看{@link com.wuda.yhan.util.commons.IsSetField}定义
     * @return field和column的映射
     */
    public static Map<String, String> fieldToColumn(TableEntity entity, boolean onlySetterCalledField) {
        Field[] fields = getField(entity, onlySetterCalledField);
        return fieldToColumn(fields);
    }

    /**
     * key是属性名称,value该属性对应的数据库表的列名.
     *
     * @param clazz class
     * @return field和column的映射
     */
    public static Map<String, String> fieldToColumn(Class<? extends TableEntity> clazz) {
        Field[] fields = getField(clazz);
        return fieldToColumn(fields);
    }

    /**
     * key是属性名称,value是该属性的值.
     *
     * @param entity                表对应的实体
     * @param onlySetterCalledField 是否值获取调用过setter方法的属性,
     *                              具体查看{@link com.wuda.yhan.util.commons.IsSetField}定义
     * @return field和value的映射
     */
    public static Map<String, Object> fieldToValue(TableEntity entity, boolean onlySetterCalledField) {
        Field[] fields = getField(entity, onlySetterCalledField);
        if (fields == null || fields.length == 0) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            Method getter = BeanUtils.getter(entity.getClass(), field.getName());
            Object fieldValue = null;
            try {
                fieldValue = getter.invoke(entity);
            } catch (Exception e) {
                // 自动生成的代码,肯定会有getter方法,一般不会来到这里
                logger.warn(e.getMessage(), e);
            }
            map.put(fieldName, fieldValue);
        }
        return map;
    }

    /**
     * 根据entity class找到对应的mybatis mapper类.
     *
     * @param clazz {@link TableEntity} class
     * @return 对应的mybatis mapper
     */
    public static Class<?> getMybatisMapper(Class<? extends TableEntity> clazz) {
        String entityName = clazz.getName();
        String mapperName = StringUtil.addSuffix(entityName, Constant.MAPPER_CLASS_NAME_SUFFIX);
        try {
            return Class.forName(mapperName);
        } catch (ClassNotFoundException e) {
            // 代码生成的类,一般都会找到
            throw new RuntimeException("TableEntity " + clazz + " 没有找到对应的Mapper类!", e);
        }
    }

    /**
     * 生成实例.在这个实例中,每个属性的值都是对应类型的随机值.而且是<strong>满足校验规则</strong>的. 比如:
     * <p>
     * <pre>
     * &#64;Cloumn(columnDefinition = "TINYINT UNSIGNED")
     * private Integer field;
     * </pre>
     * <p>
     * 虽然<i>field</i>数据类型是Integer,但是由于数据库中的定义是TINYINT
     * UNSIGNED,因此它的值随机从[0,256)范围内取一个数
     *
     * @param <T>   entity类
     * @param clazz 具体的某个entity class
     * @return 属性取随机值的实例
     */
    public static <T extends TableEntity> T genRandomValueInstance(Class<T> clazz) {
        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("clazz没有空的构造函数,不能实例化!clazz=" + clazz, e);
        }
        List<PojoFieldInfo> fieldInfos = BeanUtils.getFieldInfoList(clazz, false, true, onlyColumnAnnotation(),
                AnnotationContainsPolicy.CONTAINS_ALL);
        for (PojoFieldInfo fieldInfo : fieldInfos) {
            Method setter = fieldInfo.getSetter();
            Column columnAnnotation = (Column) fieldInfo.getAnnotations().get(0);
            Object value = MysqlTypeRandomValue.randomValue(columnAnnotation);
            try {
                setter.invoke(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException("setter方法调用异常,setter=" + setter, e);
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> Set<Class<T>> onlyColumnAnnotation() {
        Set<Class<T>> annotationClassSet = new HashSet<>();
        annotationClassSet.add((Class<T>) Column.class);
        return annotationClassSet;
    }

    /**
     * key是属性名称,value该属性对应的数据库表的列名.
     *
     * @param fields {@link TableEntity}类的属性
     * @return field-column mapping
     */
    private static Map<String, String> fieldToColumn(Field[] fields) {
        if (fields == null || fields.length == 0) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (Field field : fields) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = columnAnnotation.name();
            map.put(field.getName(), columnName);
        }
        return map;
    }

    /**
     * 获取{@link TableEntity}的属性,<strong>注意</strong>不包含
     * {@link com.wuda.yhan.util.commons.IsSetField}标记的属性.
     *
     * @param entity                表对应的实体
     * @param onlySetterCalledField 是否值获取调用过setter方法的属性,
     *                              具体查看{@link com.wuda.yhan.util.commons.IsSetField}定义
     * @return fields
     */
    private static Field[] getField(TableEntity entity, boolean onlySetterCalledField) {
        Field[] fields;
        if (onlySetterCalledField) {
            fields = IsSetFieldUtil.setterCalledFields(entity);
        } else {
            fields = getField(entity.getClass());
        }
        return fields;
    }

    /**
     * 获取{@link TableEntity}的属性,<strong>注意</strong>不包含
     * {@link com.wuda.yhan.util.commons.IsSetField}标记的属性.
     *
     * @param clazz class
     * @return fields
     */
    private static Field[] getField(Class<? extends TableEntity> clazz) {
        List<PojoFieldInfo> fieldInfoList = BeanUtils.getFieldInfoList(clazz, false, false, onlyColumnAnnotation(), AnnotationContainsPolicy.CONTAINS_ALL);
        if (fieldInfoList == null || fieldInfoList.isEmpty()) {
            return null;
        }
        Field[] fields = new Field[fieldInfoList.size()];
        for (int i = 0; i < fieldInfoList.size(); i++) {
            fields[i] = fieldInfoList.get(i).getField();
        }
        return fields;
    }

}
