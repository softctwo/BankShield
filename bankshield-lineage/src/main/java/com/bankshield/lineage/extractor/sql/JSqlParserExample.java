package com.bankshield.lineage.extractor.sql;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.schema.Table;
import com.bankshield.lineage.vo.LineageInfo;
import com.bankshield.lineage.enums.NodeType;
import com.bankshield.lineage.enums.RelationshipType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * JSqlParser集成示例
 * 展示如何使用JSqlParser重新实现SQL血缘提取功能
 *
 * @author BankShield
 * @since 2024-12-25
 */
@Slf4j
public class JSqlParserExample {

    /**
     * 使用JSqlParser解析SQL并提取血缘信息
     * 
     * @param sql SQL语句
     * @return 血缘信息
     */
    public LineageInfo extractWithJSqlParser(String sql) {
        try {
            // 解析SQL语句
            Statement statement = CCJSqlParserUtil.parse(sql);
            
            if (statement instanceof Select) {
                return extractSelectLineage((Select) statement);
            }
            
            log.info("暂不支持该SQL类型的解析: {}", statement.getClass().getSimpleName());
            return null;
            
        } catch (Exception e) {
            log.error("JSqlParser解析失败: {}", sql, e);
            return null;
        }
    }

    /**
     * 提取SELECT语句的血缘信息
     */
    private LineageInfo extractSelectLineage(Select select) {
        LineageInfo lineageInfo = new LineageInfo();
        
        try {
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            
            // 提取源表
            List<LineageInfo.TableInfo> sourceTables = extractSourceTables(plainSelect);
            lineageInfo.setSourceTables(sourceTables);
            
            // 提取目标列
            List<LineageInfo.ColumnInfo> targetColumns = extractTargetColumns(plainSelect);
            lineageInfo.setTargetColumns(targetColumns);
            
            lineageInfo.setStatementType("SELECT");
            lineageInfo.setRelationshipType(RelationshipType.DIRECT.getCode());
            
            log.info("成功提取SELECT语句血缘信息，源表数量: {}", sourceTables.size());
            
        } catch (Exception e) {
            log.error("提取SELECT血缘失败", e);
        }
        
        return lineageInfo;
    }

    /**
     * 提取源表信息
     */
    private List<LineageInfo.TableInfo> extractSourceTables(PlainSelect plainSelect) {
        List<LineageInfo.TableInfo> tables = new ArrayList<>();
        
        try {
            // 获取FROM子句中的表
            Table fromTable = (Table) plainSelect.getFromItem();
            if (fromTable != null) {
                LineageInfo.TableInfo tableInfo = new LineageInfo.TableInfo();
                tableInfo.setTableName(fromTable.getName());
                tableInfo.setNodeType(NodeType.TABLE.getCode());
                if (fromTable.getAlias() != null) {
                    tableInfo.setAlias(fromTable.getAlias().getName());
                }
                tables.add(tableInfo);
            }
            
        } catch (Exception e) {
            log.error("提取源表失败", e);
        }
        
        return tables;
    }

    /**
     * 提取目标列信息
     */
    private List<LineageInfo.ColumnInfo> extractTargetColumns(PlainSelect plainSelect) {
        List<LineageInfo.ColumnInfo> columns = new ArrayList<>();
        
        try {
            List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
            
            for (SelectItem item : selectItems) {
                LineageInfo.ColumnInfo columnInfo = new LineageInfo.ColumnInfo();
                columnInfo.setColumnName(item.toString());
                columnInfo.setNodeType(NodeType.COLUMN.getCode());
                columns.add(columnInfo);
            }
            
        } catch (Exception e) {
            log.error("提取目标列失败", e);
        }
        
        return columns;
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        JSqlParserExample example = new JSqlParserExample();
        
        String sql = "SELECT u.name, u.email FROM users u WHERE u.id = 1";
        LineageInfo result = example.extractWithJSqlParser(sql);
        
        if (result != null) {
            System.out.println("成功提取血缘信息:");
            System.out.println("语句类型: " + result.getStatementType());
            System.out.println("源表数量: " + result.getSourceTables().size());
            System.out.println("目标列数量: " + result.getTargetColumns().size());
            
            if (!result.getSourceTables().isEmpty()) {
                System.out.println("源表: " + result.getSourceTables().get(0).getTableName());
            }
        } else {
            System.out.println("血缘提取失败");
        }
    }
}