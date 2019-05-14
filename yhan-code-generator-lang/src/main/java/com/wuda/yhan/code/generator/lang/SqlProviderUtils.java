package com.wuda.yhan.code.generator.lang;

import com.wuda.yhan.util.commons.BeanUtils;
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
     * batch insert,并且希望取回数据库自增列的值,不能提前设置,
     * 否则会出现混乱.为了严格防止这种情况发生,不使用<i>script</i>
     * 方式,而是使用动态sql,目的是为了检查传入的记录中,<i>AUTO_INCREMENT</i>
     * 列对应的属性是否设置了值.
     *
     * @param sql                 sql
     * @param list                list of entity
     * @param collectionName      集合名称,类似于Mybatis foreach中的定义
     * @param autoIncrementColumn AUTO_INCREMENT column
     */
    public static void batchInsertUseGeneratedKeysColumnsAndValues(SQL sql,
                                                                   List<? extends TableEntity> list,
                                                                   String collectionName,
                                                                   String autoIncrementColumn) {
        Map<String, String> fieldToColumnMap = TableEntityUtils.fieldToColumn(list.get(0).getClass());
        Set<Map.Entry<String, String>> entrySet = fieldToColumnMap.entrySet();
        // auto-increment 列排除
        String[] columns = new String[fieldToColumnMap.size() - 1];
        int index = 0;
        String columnName;
        String fieldName;
        String columnValuePlaceholder;
        StringBuilder valueStatementTemplateBuilder = new StringBuilder();
        String placeholder = "-k-";
        for (Map.Entry<String, String> entry : entrySet) {
            columnName = entry.getValue();
            if (columnName.equalsIgnoreCase(autoIncrementColumn)) {
                continue;
            }
            columns[index] = columnName;
            fieldName = JavaNamingUtil.toCamelCase(columnName, Constant.word_separator);
            columnValuePlaceholder = "#{" + collectionName + "[" + placeholder + "]." + fieldName + "}";
            valueStatementTemplateBuilder.append(columnValuePlaceholder);
            if (index != columns.length - 1) {
                valueStatementTemplateBuilder.append(",");
            }
            index++;
        }
        String valueStatementTemplate = valueStatementTemplateBuilder.toString();
        int k = 0;
        StringBuilder builder = new StringBuilder();
        String autoIncrementField = JavaNamingUtil.toCamelCase(autoIncrementColumn, Constant.word_separator);
        for (TableEntity entity : list) {
            if (k != 0) {
                builder.append("(");
            }
            Object autoIncrementColumnValue = BeanUtils.getValue(entity, autoIncrementField);
            if (autoIncrementColumnValue != null) {
                throw new RuntimeException("batch insert,从数据库取回自增值时" +
                        ",必须使用数据库自增,不能提前设置值,否则会出现混乱" +
                        ".column=" + autoIncrementColumn + " 设置了值" +
                        ".可以考虑使用" + Constant.MAPPER_BATCH_INSERT);
            }
            String value = valueStatementTemplate.replaceAll(placeholder, k + "");
            builder.append(value);
            if (k != list.size() - 1) {
                builder.append("),");
            }
            k++;
        }
        sql.INTO_COLUMNS(columns);
        sql.INTO_VALUES(builder.toString());
    }

    /**
     * sql update语法中<i>SET</i>内容.
     *
     * @param sql                  {@link SQL}
     * @param fieldToColumn        key是属性名称,value是属性对应的数据库表的列名
     * @param parameterName        参数名称,Mybatis的参数名称
     * @param exclusiveWhereClause 排除更新条件.比如根据主键更新时,主键字段也设置了值,但是主键是作为条件,而不是更新字段,必须排除
     */
    public static void updateSetColumnsAndValues(SQL sql, Map<String, String> fieldToColumn, String parameterName, String... exclusiveWhereClause) {
        Set<Map.Entry<String, String>> entrySet = fieldToColumn.entrySet();
        String column;
        String field;
        for (Map.Entry<String, String> entry : entrySet) {
            column = entry.getValue();
            if (contains(exclusiveWhereClause, column)) {
                continue;
            }
            field = entry.getKey();
            sql.SET(column + "=#{" + parameterName + "." + field + "}");
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
     * 动态组织sql 语法中的<i>WHERE</i>条件部分,类似Mybatis的foreach.
     *
     * @param sql          {@link SQL}
     * @param whereClauses where条件中的字段
     */
    public static void whereConditionsForeach(SQL sql, String collectionName, int collectionSize, String... whereClauses) {
        String columnName;
        StringBuilder stringBuilder = new StringBuilder();
        if (whereClauses.length == 1) {
            columnName = whereClauses[0];
            stringBuilder.append(columnName);
            stringBuilder.append(" IN (");
            for (int index = 0; index < collectionSize; index++) {
                stringBuilder.append("#{");
                stringBuilder.append(collectionName);
                stringBuilder.append("[").append(index).append("]");
                stringBuilder.append("}");
                if (index != collectionSize - 1) {
                    stringBuilder.append(",");
                }
            }
            stringBuilder.append(" )");
        } else {
            String fieldName;
            for (int index = 0; index < collectionSize; index++) {
                stringBuilder.append("(");
                for (int k = 0; k < whereClauses.length; k++) {
                    columnName = whereClauses[k];
                    fieldName = JavaNamingUtil.toCamelCase(columnName, Constant.word_separator);
                    stringBuilder.append(columnName).append("=")
                            .append("#{")
                            .append(collectionName)
                            .append("[").append(index).append("].")
                            .append(fieldName)
                            .append("}");
                    if (k != whereClauses.length - 1) {
                        stringBuilder.append(" AND ");
                    }
                }
                stringBuilder.append(")");
                if (index != collectionSize - 1) {
                    stringBuilder.append(" OR ");
                }
            }
        }
        sql.WHERE(stringBuilder.toString());
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
     * 追加for update.
     *
     * @param builder sql statement
     */
    public static void appendForUpdate(StringBuilder builder) {
        builder.append(" ").append(Constant.FOR_UPDATE_STATEMENT);
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
