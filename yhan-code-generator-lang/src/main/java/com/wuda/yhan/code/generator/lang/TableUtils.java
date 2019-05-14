package com.wuda.yhan.code.generator.lang;

import com.wuda.yhan.code.generator.lang.relational.Index;
import com.wuda.yhan.code.generator.lang.relational.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.wuda.yhan.code.generator.lang.relational.Table}的工具类.
 *
 * @author wuda
 */
public class TableUtils {

    /**
     * 获取table中的所有唯一索引.不包含主键.
     *
     * @param table table
     * @return 唯一索引列表
     */
    public static List<Index> getUniqueIndices(Table table) {
        return getIndices(table, true);
    }

    /**
     * 获取table中的所有非唯一索引.不包含主键.
     *
     * @param table table
     * @return 所有的非唯一索引列表
     */
    public static List<Index> getNonUniqueIndices(Table table) {
        return getIndices(table, false);
    }

    /**
     * 获取table中的索引.不包含主键.
     *
     * @param table  table
     * @param unique 是否唯一索引
     * @return 索引列表
     */
    public static List<Index> getIndices(Table table, boolean unique) {
        List<Index> indices = table.getIndices();
        if (indices == null || indices.isEmpty()) {
            return null;
        }
        List<Index> list = new ArrayList<>(indices.size());
        for (Index index : indices) {
            if (unique && index.getType() == Index.Type.UNIQUE) {
                list.add(index);
            } else if (!unique && index.getType() != Index.Type.UNIQUE) {
                list.add(index);
            }
        }
        return list;
    }

}
