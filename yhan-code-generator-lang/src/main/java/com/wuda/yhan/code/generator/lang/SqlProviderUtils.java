package com.wuda.yhan.code.generator.lang;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;
import java.util.Set;

/**
 * mybatis SqlProvider 工具类.
 *
 * @author wuda
 */
public class SqlProviderUtils {

    /**
     * sql insert语法中指定列名和<code>VALUES</code>.
     *
     * @param sql
     *         {@link SQL}
     * @param fieldToColumn
     *         key是属性名称,value是属性对应的数据库表的列名
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
}
