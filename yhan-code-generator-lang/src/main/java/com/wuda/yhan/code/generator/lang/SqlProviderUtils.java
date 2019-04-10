package com.wuda.yhan.code.generator.lang;

import com.wuda.yhan.util.commons.JavaNamingUtil;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * mybatis SqlProvider 工具类.
 *
 * @author wuda
 */
public class SqlProviderUtils {

    /**
     * sql insert语法中指定列名和<i>VALUES</i>.
     *
     * @param sql           {@link SQL}
     * @param fieldToColumn key是属性名称,value是属性对应的数据库表的列名
     */
    public static void insertColumnsAndValues(SQL sql, Map<String, String> fieldToColumn) {
        Set<Map.Entry<String, String>> entrySet = fieldToColumn.entrySet();
        String[] columns = new String[fieldToColumn.size()];
        String[] values = new String[fieldToColumn.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : entrySet) {
            columns[index] = entry.getValue();
            values[index] = "#{" + entry.getKey() + "}";
            index++;
        }
        sql.INTO_COLUMNS(columns);
        sql.INTO_VALUES(values);
    }

    /**
     * sql update语法中<i>SET</i>内容.
     *
     * @param sql           {@link SQL}
     * @param fieldToColumn key是属性名称,value是属性对应的数据库表的列名
     */
    public static void updateSetColumnsAndValues(SQL sql, Map<String, String> fieldToColumn) {
        Set<Map.Entry<String, String>> entrySet = fieldToColumn.entrySet();
        String column;
        String field;
        for (Map.Entry<String, String> entry : entrySet) {
            column = entry.getValue();
            field = entry.getKey();
            sql.SET(column + "=#{" + field + "}");
        }
    }

    /**
     * 校验实体类中已经调用过<strong>set</strong>方法的属性.
     *
     * @param entity                       实体类
     * @param setterCalledFieldToColumnMap 实体类中调用过set方法的属性集.key-调用过set方法的属性,value-属性对应的表的列
     */
    public static void setterCalledFieldValidate(TableEntity entity, Map<String, String> setterCalledFieldToColumnMap) {
        if (setterCalledFieldToColumnMap == null || setterCalledFieldToColumnMap.size() == 0) {
            throw new RuntimeException("没有属性调用过set方法! Class Name:" + entity.getClass().getName());
        }
    }

    /**
     * sql update语句中的set内容,排除掉不需要更新的列.
     *
     * @param fieldToColumn    key是属性名称,value是属性对应的数据库表的列名
     * @param exclusiveColumns 排除掉不更新的字段,比如根据主键更新时,主键字段也设置了值,但是主键是作为条件,而不是更新字段,必须排除
     */
    public static void exclusiveUpdateColumns(Map<String, String> fieldToColumn, String... exclusiveColumns) {
        fieldToColumn.forEach((field, column) -> {
            if (contains(exclusiveColumns, column)) {
                fieldToColumn.remove(field, column);
            }
        });
    }

    /**
     * 数组中是否包含给定的元素.
     *
     * @param array   array
     * @param element element
     * @return true-如果包含
     */
    private static boolean contains(String[] array, String element) {
        if (array != null && array.length > 0 && element != null) {
            for (String e : array) {
                if (e.equals(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 验证.
     *
     * @param entity entity
     */
    public static void validate(TableEntity entity) {

    }

    /**
     * 验证查询返回的字段.比如不能为"*".
     *
     * @param columns retrieve columns
     */
    public static void selectColumnsValidate(String... columns) {
        if (columns == null || columns.length == 0) {
            throw new RuntimeException("必须指定需要返回的列!");
        }
    }

    /**
     * sql 语法中的<i>WHERE</i>条件部分.
     *
     * @param sql          {@link SQL}
     * @param whereClauses where条件中的字段
     */
    public static void whereConditions(SQL sql, String... whereClauses) {
        String fieldName;
        for (String columnName : whereClauses) {
            fieldName = JavaNamingUtil.toCamelCase(columnName, Constant.word_separator);
            sql.WHERE(columnName + "=#{" + fieldName + "}");
        }
    }

    /**
     * 追加分页参数.
     *
     * @param builder sql statement
     */
    public static void appendPaging(StringBuilder builder) {
        builder.append(" LIMIT #{").append(Constant.PAGING_OFFSET).append("},#{").append(Constant.PAGING_ROW_COUNT).append("}");
    }

    /**
     * Returns a comma-separated String representation of this list, quoting all
     * name parts with the {@code "} char.
     *
     * @param list list of string
     * @return literal
     */
    public static String toDoubleQuotedString(List<String> list) {
        char quotingChar = '"';
        StringBuilder builder = new StringBuilder();
        for (String element : list) {
            builder.append(quotingChar);
            builder.append(element);
            builder.append(quotingChar);
            builder.append(",");
        }
        return builder.substring(0, builder.length() - 1);
    }
}
