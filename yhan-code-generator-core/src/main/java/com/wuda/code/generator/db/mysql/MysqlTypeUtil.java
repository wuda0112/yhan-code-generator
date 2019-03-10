package com.wuda.code.generator.db.mysql;

import com.mysql.cj.MysqlType;
import com.wuda.code.generator.CodeGenerateException;

/**
 * mysql type util.
 *
 * @author wuda
 */
public class MysqlTypeUtil {

    /**
     * mysql 数据类型对应的java类型.
     *
     * @param fullMysqlTypeName
     *         full MySQL type name
     * @return java class
     */
    public static Class<?> mysqlTypeToJavaType(String fullMysqlTypeName) {
        MysqlType mysqlType = MysqlType.getByName(fullMysqlTypeName);
        try {
            return Class.forName(mysqlType.getClassName());
        } catch (ClassNotFoundException e) {
            // 使用的是mysql java驱动的类,不会来到这里
            throw new CodeGenerateException(e);
        }
    }
}
