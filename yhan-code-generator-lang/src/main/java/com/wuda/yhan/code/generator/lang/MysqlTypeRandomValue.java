package com.wuda.yhan.code.generator.lang;

import com.mysql.cj.MysqlType;
import com.wuda.yhan.util.commons.CharacterUtils;
import com.wuda.yhan.util.commons.RandomUtilsExt;
import com.wuda.yhan.util.commons.keygen.KeyGeneratorSnowflake;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class MysqlTypeRandomValue {

    private static Logger logger = LoggerFactory.getLogger(MysqlTypeRandomValue.class);

    /**
     * 唯一key生成器.
     */
    static KeyGeneratorSnowflake snowflake = new KeyGeneratorSnowflake(0);

    /**
     * 快捷方法,统一处理异常.
     *
     * @return next key
     */
    static long next() {
        try {
            return snowflake.next();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据mysql column的定义,返回对应的随机值.
     *
     * @param column
     *         jpa column
     * @return 随机值
     */
    public static Object randomValue(Column column) {
        MysqlType mysqlType = MysqlType.getByName(column.columnDefinition());
        int length = column.length();
        Object value = "mysqlType=" + mysqlType + ",目前还不支持生成随机值";
        switch (mysqlType) {
            case BIGINT:
                value = next();
                break;
            case BIGINT_UNSIGNED:
                value = next();
                break;
            case BINARY:
                break;
            case BIT:
                break;
            case BLOB:
                break;
            case BOOLEAN:
                value = RandomUtils.nextBoolean();
                break;
            case CHAR:
                value = RandomUtilsExt.randomString(length);
                break;
            case DATE:
                value = new Date(System.currentTimeMillis());
                break;
            case DATETIME:
                value = new Timestamp(System.currentTimeMillis());
                break;
            case DECIMAL:
                break;
            case DECIMAL_UNSIGNED:
                break;
            case DOUBLE:
                break;
            case DOUBLE_UNSIGNED:
                break;
            case ENUM:
                break;
            case FLOAT:
                break;
            case FLOAT_UNSIGNED:
                break;
            case GEOMETRY:
                break;
            case INT:
                value = RandomUtils.nextInt();
                break;
            case INT_UNSIGNED:
                value = RandomUtils.nextInt(0, Integer.MAX_VALUE);
                break;
            case JSON:
                break;
            case LONGBLOB:
                value = RandomUtils.nextBytes(length);
                break;
            case LONGTEXT:
                value = RandomUtilsExt.randomString(length);
                break;
            case MEDIUMBLOB:
                value = RandomUtils.nextBytes(length);
                break;
            case MEDIUMINT:
                break;
            case MEDIUMINT_UNSIGNED:
                break;
            case MEDIUMTEXT:
                value = RandomUtilsExt.randomString(length);
                break;
            case NULL:
                break;
            case SET:
                break;
            case SMALLINT:
                break;
            case SMALLINT_UNSIGNED:
                break;
            case TEXT:
                value = RandomUtilsExt.randomString(length);
                break;
            case TIME:
                value = new Time(System.currentTimeMillis());
                break;
            case TIMESTAMP:
                value = new Timestamp(System.currentTimeMillis());
                break;
            case TINYBLOB:
                value = RandomUtils.nextBytes(length);
                break;
            case TINYINT:
                value = RandomUtils.nextInt(0, 128);
                break;
            case TINYINT_UNSIGNED:
                value = RandomUtils.nextInt(0, 256);
                break;
            case TINYTEXT:
                value = RandomUtilsExt.randomString(length);
                break;
            case UNKNOWN:
                break;
            case VARBINARY:
                break;
            case VARCHAR:
                value = RandomUtilsExt.randomString(length, CharacterUtils.UNICODE_HAN_START, CharacterUtils.UNICODE_HAN_END);
                break;
            case YEAR:
                break;
            default:
                break;

        }
        return value;
    }

}
