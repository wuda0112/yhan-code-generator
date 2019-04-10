package com.wuda.yhan;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUnique;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlTableIndex;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlCreateTableParser;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.wuda.yhan.code.generator.lang.relational.Column;
import com.wuda.yhan.code.generator.lang.relational.Table;
import io.debezium.connector.mysql.MySqlDdlParser;
import io.debezium.relational.TableId;
import io.debezium.relational.Tables;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TableTest extends TestBase {


    public List<Table> getTable() {
        Tables tables = new Tables();

        MySqlDdlParser parser = new MySqlDdlParser();
        parser.parse(ddl, tables);
        Set<TableId> tableIds = tables.tableIds();
        List<Table> tableList = new ArrayList<>(tableIds.size());
        for (TableId tableId : tableIds) {
            io.debezium.relational.Table actualTable = tables.forTable(tableId);
            Table table = new Table();
            table.setActualTable(actualTable);
            tableList.add(table);
        }
        return tableList;
    }

    @Test
    public void test() {
        List<Table> tables = getTable();
        for (Table table : tables) {
            List<Column> columns = table.nonPrimaryKeyColumns();
            printColumn(columns);
        }
    }

    private void printColumn(List<Column> columns) {
        columns.forEach(column -> {
            System.out.println(column.name());
        });
    }

    /**
     * 在一个脚本中有多个create table,则会分别解析出来.
     */
    @Test
    public void mySqlStatementParserTest() {
        List<SQLStatement> statementList = parseStatementList();
        for (SQLStatement statement : statementList) {
            System.out.println(statement);
        }
    }

    /**
     * 在一个脚本中有多个create table,则会分别解析出来.
     * 一个Create Table语句就是一个SQLStatement
     *
     * @return
     */
    private List<SQLStatement> parseStatementList() {
        MySqlStatementParser parser = new MySqlStatementParser(ddl);
        return parser.parseStatementList();
    }

    @Test
    public void mySqlCreateTableParserTest() {
        List<SQLStatement> statementList = parseStatementList();
        for (SQLStatement statement : statementList) {
            MySqlCreateTableStatement createTableStatement = (MySqlCreateTableStatement) statement;
            SQLExprTableSource tableSource = createTableStatement.getTableSource();
            System.out.println(tableSource.getSchema() + "." + tableSource.getName().getSimpleName());
            List<SQLTableElement> list = createTableStatement.getTableElementList();
            for (SQLTableElement element : list) {
                System.out.println(element);
            }
        }
    }

    /**
     * 一次只能解析一个Create Table语句.
     */
    @Test
    public void parseIndexTest() {
        List<SQLStatement> statementList = parseStatementList();
        for (SQLStatement statement : statementList) {
            MySqlCreateTableParser parser = new MySqlCreateTableParser(statement.toString());
            SQLCreateTableStatement createTableStatement = parser.parseCreateTable();
            List<SQLTableElement> list = createTableStatement.getTableElementList();
            for (SQLTableElement element : list) {
                if (element instanceof MySqlTableIndex) {
                    mySqlTableIndexTest((MySqlTableIndex) element);
                } else if (element instanceof MySqlUnique) {
                    MySqlUnique mySqlUnique = (MySqlUnique) element;
                    mySqlUniqueTest(mySqlUnique);
                }
            }
        }
    }

    // 这里一个element就代表一个索引
    private void mySqlTableIndexTest(MySqlTableIndex indexElement) {

        System.out.println(indexElement);
        // 组成索引的所有列
        List<SQLSelectOrderByItem> indexColumns = indexElement.getColumns();
        for (SQLSelectOrderByItem item : indexColumns) {
            SQLExpr expr = item.getExpr();
            // 索引中的列的信息
            SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) expr;
            System.out.println(sqlIdentifierExpr.getSimpleName());
        }
    }

    // 这里一个element就代表一个索引
    private void mySqlUniqueTest(MySqlUnique indexElement) {

        System.out.println(indexElement);
        // 组成索引的所有列
        List<SQLSelectOrderByItem> indexColumns = indexElement.getColumns();
        for (SQLSelectOrderByItem item : indexColumns) {
            SQLExpr expr = item.getExpr();
            // 索引中的列的信息
            SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) expr;
            System.out.println(sqlIdentifierExpr.getSimpleName());
        }
    }

}
