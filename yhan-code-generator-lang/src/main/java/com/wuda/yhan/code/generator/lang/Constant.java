package com.wuda.yhan.code.generator.lang;

/**
 * 常量.
 *
 * @author wuda
 */
public class Constant {

    /**
     * 单词之间的分隔符.通常很多表名或者列名用下划线分隔,比如: product_name.
     */
    public static char word_separator = '_';

    /**
     * mapper中insert方法的名称.
     */
    public final static String MAPPER_INSERT = "insert";

    /**
     * mapper中insert方法的名称.
     */
    public final static String MAPPER_INSERT_USE_GENERATED_KEYS = "insertUseGeneratedKeys";

    /**
     * mapper中batch insert方法的名称.
     */
    public final static String MAPPER_BATCH_INSERT = "batchInsert";

    /**
     * mapper中batch insert方法的名称.
     */
    public final static String MAPPER_BATCH_INSERT_USE_GENERATED_KEYS = "batchInsertUseGeneratedKeys";
    /**
     * mapper中deleteByPrimaryKey方法的名称.
     */
    public final static String MAPPER_DELETE_BY_PRIMARY_KEY = "deleteByPrimaryKey";
    /**
     * mapper中updateByPrimaryKey方法的名称.
     */
    public final static String MAPPER_UPDATE_BY_PRIMARY_KEY = "updateByPrimaryKey";
    /**
     * mapper中selectByPrimaryKey方法的名称.
     */
    public final static String MAPPER_SELECT_BY_PRIMARY_KEY = "selectByPrimaryKey";

    /**
     * mapper中batchSelectByPrimaryKey方法的名称.
     */
    public final static String MAPPER_BATCH_SELECT_BY_PRIMARY_KEY = "batchSelectByPrimaryKey";

    /**
     * mapper类名称的后缀.
     */
    public final static String MAPPER_CLASS_NAME_SUFFIX = "Mapper";
    /**
     * 分页中的offset.
     */
    public final static String PAGING_OFFSET = "offset";
    /**
     * 分页中的row count.
     */
    public final static String PAGING_ROW_COUNT = "rowCount";
    /**
     * SQL 语句中的select count.
     */
    public final static String COUNT_STATEMENT = "COUNT(*)";

    /**
     * selectBy前缀.
     */
    public final static String SELECT_BY_PREFIX = "selectBy";

    /**
     * batchSelectBy前缀.
     */
    public final static String BATCH_SELECT_BY_PREFIX = "batchSelectBy";

    /**
     * countBy前缀.
     */
    public final static String COUNT_BY_PREFIX = "countBy";

    /**
     * retrieveColumns.
     */
    public final static String RETRIEVE_COLUMNS="retrieveColumns";

}
