package com.wuda.yhan;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUnique;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlTableIndex;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.wuda.yhan.code.generator.lang.relational.Index;
import com.wuda.yhan.code.generator.lang.relational.Table;
import io.debezium.connector.mysql.MySqlDdlParser;
import io.debezium.relational.TableId;
import io.debezium.relational.Tables;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MySql Create Table DDL parser.
 * 包装了其他两个Parser,其中{@link MySqlDdlParser Debezium MySqlDdlParser}
 * 用于解析<i>Create Table</i>中的主要信息,比如表名,列,主键等,但是不包含索引信息;
 * {@link MySqlStatementParser Druid MySqlStatementParser}作为补充,
 * 用于解析表中的索引信息.
 *
 * @author wuda
 */
public class MySqlCreateTableStatementParser {

    /**
     * 包装{@link MySqlDdlParser Debezium MySqlDdlParser}.
     */
    private MySqlDdlParser mainParser = new MySqlDdlParser();

    /**
     * 根据Create Table DDL statement,解析出对应的{@link Table}实例.
     *
     * @param createTableContent Create Table 语句
     * @return list of table;或者<code>null</code>,如果没有解析出任何内容
     */
    public List<Table> parse(String createTableContent) {
        if (createTableContent == null || createTableContent.isEmpty()) {
            return null;
        }
        Map<String, Table> tableMap = mainParse(createTableContent);
        if (tableMap == null || tableMap.isEmpty()) {
            return null;
        }
        Map<String, List<Index>> indexMap = indexParse(createTableContent);
        tableAppendIndex(tableMap, indexMap);
        return toTableList(tableMap);
    }

    private List<Table> toTableList(Map<String, Table> tableMap) {
        List<Table> list = new ArrayList<>(tableMap.size());
        for (Map.Entry<String, Table> entry : tableMap.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }

    /**
     * key是schemaDotTable,value是{@link Table}.
     *
     * @param createTableContent Create Table DDL 语句
     * @return name and table mapping
     */
    private Map<String, Table> mainParse(String createTableContent) {
        Tables tables = new Tables();
        mainParser.parse(createTableContent, tables);
        return tableMap(tables);
    }

    /**
     * key是schemaDotTable,value是这个表中的索引,不包含主键.
     *
     * @param createTableContent Create Table DDL 语句
     * @return table name and table indices mapping
     */
    private Map<String, List<Index>> indexParse(String createTableContent) {
        List<SQLStatement> statements = parseStatementList(createTableContent);
        if (statements == null || statements.isEmpty()) {
            return null;
        }
        List<MySqlCreateTableStatement> createTableStatements = filterCreateTableStatement(statements);
        if (createTableStatements == null || createTableStatements.isEmpty()) {
            return null;
        }
        Map<String, List<Index>> tableIndex = new HashMap<>(createTableStatements.size());
        for (MySqlCreateTableStatement statement : createTableStatements) {
            List<Index> indices = parseIndex(statement);
            String schemaDotTable = getSchemaDotTable(statement);
            if (indices != null && !indices.isEmpty()) {
                tableIndex.put(schemaDotTable, indices);
            }
        }
        return tableIndex;
    }

    /**
     * schema.table
     *
     * @param statement {@link MySqlCreateTableStatement}
     * @return schema.table
     */
    private String getSchemaDotTable(MySqlCreateTableStatement statement) {
        SQLExprTableSource tableSource = statement.getTableSource();
        StringBuilder builder = new StringBuilder();
        String schema = tableSource.getSchema();
        if (schema != null && !schema.isEmpty()) {
            schema = SQLUtils.normalize(schema);
            builder.append(schema).append(".");
        }
        String table = SQLUtils.normalize(tableSource.getName().getSimpleName());
        builder.append(table);
        return builder.toString();
    }

    /**
     * 解析任意DDL语句,不止Create Table语句.
     *
     * @param ddlContent DDL
     * @return list of statement
     */
    private List<SQLStatement> parseStatementList(String ddlContent) {
        MySqlStatementParser parser = new MySqlStatementParser(ddlContent);
        return parser.parseStatementList();
    }

    /**
     * 过滤出{@link MySqlCreateTableStatement}.
     *
     * @param sqlStatements {@link SQLStatement}s
     * @return list of {@link MySqlCreateTableStatement}
     */
    public List<MySqlCreateTableStatement> filterCreateTableStatement(List<SQLStatement> sqlStatements) {
        if (sqlStatements == null || sqlStatements.isEmpty()) {
            return null;
        }
        return sqlStatements
                .stream()
                .filter(sqlStatement -> sqlStatement instanceof MySqlCreateTableStatement)
                .map(sqlStatement -> (MySqlCreateTableStatement) sqlStatement)
                .collect(Collectors.toList());
    }

    /**
     * 解析这个Create Table语句中索引的信息,不包含主键.
     *
     * @param statement 一个Create Table Statement
     * @return 表中所有的索引
     */
    public List<Index> parseIndex(MySqlCreateTableStatement statement) {
        List<SQLTableElement> list = statement.getTableElementList();
        if (list == null || list.isEmpty()) {
            return null;
        }
        List<Index> indices = new ArrayList<>(list.size());
        for (SQLTableElement element : list) {
            if (element instanceof MySqlTableIndex) {
                MySqlTableIndex tableIndex = (MySqlTableIndex) element;
                String indexName = SQLUtils.normalize(tableIndex.getName().getSimpleName());
                Index index = newIndex(indexName, Index.Type.KEY, tableIndex.getColumns());
                indices.add(index);
            } else if (element instanceof MySqlUnique) {
                // UNIQUE index
                MySqlUnique uniqueIndex = (MySqlUnique) element;
                String indexName = SQLUtils.normalize(uniqueIndex.getName().getSimpleName());
                Index index = newIndex(indexName, Index.Type.UNIQUE, uniqueIndex.getColumns());
                indices.add(index);
            }
        }
        return indices;
    }

    private Index newIndex(String indexName, Index.Type indexType, List<SQLSelectOrderByItem> indexColumns) {
        String[] columns = new String[indexColumns.size()];
        for (int i = 0; i < indexColumns.size(); i++) {
            SQLSelectOrderByItem item = indexColumns.get(i);
            SQLExpr expr = item.getExpr();
            // 索引中的列的信息
            SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) expr;
            columns[i] = sqlIdentifierExpr.normalizedName();
        }
        return new Index(indexName, indexType, columns);
    }

    private Map<String, Table> tableMap(Tables tables) {
        Set<TableId> tableIds = tables.tableIds();
        Map<String, Table> tableMap = new HashMap<>(tableIds.size());
        for (TableId tableId : tableIds) {
            io.debezium.relational.Table actualTable = tables.forTable(tableId);
            Table table = new Table();
            table.setActualTable(actualTable);
            tableMap.put(tableId.toString(), table);
        }
        return tableMap;
    }

    /**
     * 将索引信息设置到对应的{@link Table}实例中.
     *
     * @param tableMap table map
     * @param indexMap 索引
     */
    private void tableAppendIndex(Map<String, Table> tableMap, Map<String, List<Index>> indexMap) {
        if (tableMap == null || tableMap.isEmpty()) {
            return;
        }
        if (indexMap == null || indexMap.isEmpty()) {
            return;
        }
        indexMap.forEach((tableName, indices) -> {
            Table table = tableMap.get(tableName);
            if (table != null) {
                table.setIndices(indices);
            }
        });
    }
}
