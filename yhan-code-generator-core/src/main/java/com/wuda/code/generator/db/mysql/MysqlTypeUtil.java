package com.wuda.code.generator.db.mysql;

import com.mysql.cj.MysqlType;
import com.wuda.code.generator.CodeGenerateException;

import java.sql.JDBCType;

/**
 * mysql type util.
 *
 * @author wuda
 */
public class MysqlTypeUtil {

    /**
     * mysql 数据类型对应的java类型.
     *
     * @param fullMysqlTypeName full MySQL type name
     * @param bigIntegerToLong  {@link MysqlType#BIGINT_UNSIGNED}对应的Java class type是{@link java.math.BigInteger},
     *                          现实应用中应该还没必要,{@link Long}类型即可.如果是true,则表示如果
     *                          数据库中的定义是{@link MysqlType#BIGINT_UNSIGNED},则转换成{@link MysqlType#INT_UNSIGNED}.
     * @return java class
     */
    public static Class<?> mysqlTypeToJavaType(String fullMysqlTypeName, boolean bigIntegerToLong) {
        MysqlType mysqlType = getMysqlType(fullMysqlTypeName, bigIntegerToLong);
        try {
            return Class.forName(mysqlType.getClassName());
        } catch (ClassNotFoundException e) {
            // 使用的是mysql java驱动的类,不会来到这里
            throw new CodeGenerateException(e);
        }
    }

    /**
     * mysql 数据类型对应的java类型.
     *
     * @param fullMysqlTypeName full MySQL type name
     * @return java class
     */
    public static Class<?> mysqlTypeToJavaType(String fullMysqlTypeName) {
        return mysqlTypeToJavaType(fullMysqlTypeName, true);
    }

    /**
     * 获取jdbc type.
     *
     * @param fullMysqlTypeName full MySQL type name
     * @param bigIntegerToLong  {@link MysqlType#BIGINT_UNSIGNED}对应的Java class type是{@link java.math.BigInteger},
     *                          现实应用中应该还没必要,{@link Long}类型即可.如果是true,则表示如果
     *                          数据库中的定义是{@link MysqlType#BIGINT_UNSIGNED},则转换成{@link MysqlType#INT_UNSIGNED}.
     * @return {@link JDBCType}
     */
    public static JDBCType toJDBCType(String fullMysqlTypeName, boolean bigIntegerToLong) {
        MysqlType mysqlType = getMysqlType(fullMysqlTypeName, bigIntegerToLong);
        int jdbcType = mysqlType.getJdbcType();
        return JDBCType.valueOf(jdbcType);
    }

    /**
     * 获取mysql type
     *
     * @param fullMysqlTypeName full MySQL type name
     * @param bigIntegerToLong  {@link MysqlType#BIGINT_UNSIGNED}对应的Java class type是{@link java.math.BigInteger},
     *                          现实应用中应该还没必要,{@link Long}类型即可.如果是true,则表示如果
     *                          数据库中的定义是{@link MysqlType#BIGINT_UNSIGNED},则转换成{@link MysqlType#INT_UNSIGNED}.
     * @return {@link MysqlType}
     */
    public static MysqlType getMysqlType(String fullMysqlTypeName, boolean bigIntegerToLong) {
        MysqlType mysqlType = MysqlType.getByName(fullMysqlTypeName);
        if (mysqlType == MysqlType.BIGINT_UNSIGNED && bigIntegerToLong) {
            mysqlType = MysqlType.INT_UNSIGNED;
        }
        return mysqlType;
    }
}
