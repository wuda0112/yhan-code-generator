package com.wuda.yhan.code.generator.lang.relational;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 数据库表的索引信息.
 *
 * @author wuda
 */
@Data
@AllArgsConstructor
public class Index {

    /**
     * 索引类型.
     */
    public enum Type {
        PRIMARY, KEY, UNIQUE, FULLTEXT, SPATIAL;
    }

    /**
     * 索引名称.
     */
    private String name;
    /**
     * 索引类型.
     */
    private Type type;
    /**
     * 组成索引的column.
     */
    private String[] columns;

}
