package com.wuda.yhan.code.generator.lang;

import lombok.Data;

/**
 * 排序参数.
 *
 * @author wuda
 */
@Data
public class OrderBy {
    
    private String column;
    private Order order = Order.ASC;

    /**
     * 排序方向.
     */
    public enum Order {
        ASC, DESC;
    }
}
