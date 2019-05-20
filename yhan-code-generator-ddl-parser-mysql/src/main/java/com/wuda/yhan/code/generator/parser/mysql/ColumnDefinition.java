package com.wuda.yhan.code.generator.parser.mysql;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * column definition.
 *
 * @author wuda
 */
@Data
@AllArgsConstructor
public class ColumnDefinition {
    /**
     * 列名.
     */
    private String columnName;
    /**
     * 注释.
     */
    private String comment;
}
