package com.wuda.yhan.code.generator.lang.util;

import com.wuda.yhan.code.generator.lang.Constant;
import com.wuda.yhan.code.generator.lang.OrderBy;
import com.wuda.yhan.code.generator.lang.TableEntity;
import org.apache.ibatis.jdbc.SQL;
import org.mybatis.dynamic.sql.SqlColumn;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        if (list == null || list.isEmpty()) {
            throw new RuntimeException("批量插入的数据不能为空");
        }
        // 同一类型,任意取一个即可
        Class<? extends TableEntity> arbitrary = list.get(0).getClass();
        Map<String, String> fieldToColumnMap = TableEntityUtils.fieldToColumn(arbitrary);
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
            fieldName = JavaNamingUtils.toCamelCase(columnName, Constant.underscore);
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
        String autoIncrementField = JavaNamingUtils.toCamelCase(autoIncrementColumn, Constant.underscore);
        Method autoIncrementFieldGetter = BeanUtils.getter(arbitrary, autoIncrementField);
        for (TableEntity entity : list) {
            if (k != 0) {
                builder.append("(");
            }
            Object autoIncrementColumnValue = BeanUtils.getValue(entity, autoIncrementFieldGetter);
            if (autoIncrementColumnValue != null) {
                throw new RuntimeException("如果想使用useGeneratedKeys特性,必须使用数据库的自增值,不能提前设置值,否则会出现混乱" +
                        ".column " + autoIncrementColumn + " 设置了值" +
                        ".如果自己设置值,可以考虑使用" + Constant.MAPPER_BATCH_INSERT +
                        "方法");
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
     * @param entity                  实体类
     * @param nonNullFieldToColumnMap 实体类中调用过set方法的属性集.key-调用过set方法的属性,value-属性对应的表的列
     */
    public static void noneNullFieldValidate(TableEntity entity, Map<String, String> nonNullFieldToColumnMap) {
        if (nonNullFieldToColumnMap == null || nonNullFieldToColumnMap.size() == 0) {
            throw new RuntimeException("所有字段都为null! Class Name:" + entity.getClass().getName());
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
     * 转换成字符串数组形式.
     *
     * @param sqlColumns list of sql column
     * @return 字符串数组形式的结果
     */
    public static String[] sqlColumnsToArray(List<SqlColumn> sqlColumns) {
        if (sqlColumns == null || sqlColumns.isEmpty()) {
            return null;
        }
        String[] columnArray = new String[sqlColumns.size()];
        StringBuilder builder = new StringBuilder();
        String alias;
        for (int i = 0; i < sqlColumns.size(); i++) {
            SqlColumn sqlColumn = sqlColumns.get(i);
            builder.append(sqlColumn.name());
            alias = getColumnAlias(sqlColumn);
            if (alias != null && !alias.isEmpty()) {
                builder.append(" AS ").append(alias);
            }
            columnArray[i] = builder.toString();
            builder.delete(0, builder.length());
        }
        return columnArray;
    }

    private static String getColumnAlias(SqlColumn sqlColumn) {
        Optional optional = sqlColumn.alias();
        if (optional.isPresent()) {
            return optional.get().toString();
        }
        return null;
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
            fieldName = JavaNamingUtils.toCamelCase(columnName, Constant.underscore);
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
                    fieldName = JavaNamingUtils.toCamelCase(columnName, Constant.underscore);
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
     * 追加排序参数.
     *
     * @param builder     sql statement
     * @param orderByList 排序参数
     */
    public static void appendOrderBy(StringBuilder builder, List<OrderBy> orderByList) {
        if (orderByList == null || orderByList.isEmpty()) {
            return;
        }
        builder.append(" ORDER BY ");
        for (int index = 0; index < orderByList.size(); index++) {
            if (index > 0) {
                builder.append(",");
            }
            OrderBy orderBy = orderByList.get(index);
            builder.append(orderBy.getColumn()).append(" ").append(orderBy.getOrder());
        }
    }

    /**
     * 追加排序参数.
     *
     * @param sql         sql statement
     * @param orderByList 排序参数
     */
    public static void appendOrderBy(SQL sql, List<OrderBy> orderByList) {
        if (orderByList == null || orderByList.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (OrderBy orderBy : orderByList) {
            builder.append(orderBy.getColumn()).append(" ").append(orderBy.getOrder());
            sql.ORDER_BY(builder.toString());
            builder.delete(0, builder.length());
        }
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

    /**
     * Returns a string quoting with the {@code "} char.
     *
     * @param str string
     * @return literal
     */
    public static String toDoubleQuotedString(String str) {
        return "\"" + str + "\"";
    }
}
