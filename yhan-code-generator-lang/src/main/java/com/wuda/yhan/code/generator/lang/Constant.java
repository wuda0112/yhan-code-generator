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
    public final static String MAPPER_INSERT="insert";
    /**
     * mapper中batch insert方法的名称.
     */
    public final static String MAPPER_BATCH_INSERT="batchInsert";
    /**
     * mapper中deleteByPrimaryKey方法的名称.
     */
    public final static String MAPPER_DELETE_BY_PRIMARY_KEY="deleteByPrimaryKey";
    /**
     * mapper中updateByPrimaryKey方法的名称.
     */
    public final static String MAPPER_UPDATE_BY_PRIMARY_KEY="updateByPrimaryKey";
    /**
     * mapper中selectByPrimaryKey方法的名称.
     */
    public final static String MAPPER_SELECT_BY_PRIMARY_KEY="selectByPrimaryKey";
    /**
     * mapper类名称的后缀.
     */
    public final static String MAPPER_CLASS_NAME_SUFFIX = "Mapper";
    /**
     * 分页中的offset.
     */
    public final static String PAGING_OFFSET="offset";
    /**
     * 分页中的row count.
     */
    public final static String PAGING_ROW_COUNT="rowCount";

}
