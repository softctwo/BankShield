package com.bankshield.lineage.extractor.sql;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitorAdapter;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGASTVisitorAdapter;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.bankshield.lineage.enums.NodeType;
import com.bankshield.lineage.enums.RelationshipType;
import com.bankshield.lineage.vo.LineageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * SQL血缘提取器
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Slf4j
@Component
public class SqlLineageExtractor {

    /**
     * 从SQL语句中提取血缘信息
     *
     * @param sql SQL语句
     * @param dbType 数据库类型
     * @return 血缘信息
     */
    public LineageInfo extractFromSql(String sql, String dbType) {
        try {
            // 解析SQL语句
            SQLStatement statement = SQLUtils.parseSingleStatement(sql, dbType);
            
            if (statement == null) {
                log.warn("无法解析SQL语句: {}", sql);
                return null;
            }

            // 根据SQL类型提取血缘信息
            if (statement instanceof SQLSelectStatement) {
                return extractSelectLineage((SQLSelectStatement) statement, dbType);
            } else if (statement instanceof SQLInsertStatement) {
                return extractInsertLineage((SQLInsertStatement) statement, dbType);
            } else if (statement instanceof SQLUpdateStatement) {
                return extractUpdateLineage((SQLUpdateStatement) statement, dbType);
            } else if (statement instanceof SQLCreateViewStatement) {
                return extractViewLineage((SQLCreateViewStatement) statement, dbType);
            }

            log.warn("不支持的SQL类型: {}", statement.getClass().getSimpleName());
            return null;

        } catch (Exception e) {
            log.error("解析SQL血缘失败: {}", sql, e);
            return null;
        }
    }

    /**
     * 提取SELECT语句的血缘信息
     */
    private LineageInfo extractSelectLineage(SQLSelectStatement statement, String dbType) {
        LineageInfo lineageInfo = new LineageInfo();
        
        try {
            SQLSelect select = statement.getSelect();
            SQLSelectQueryBlock queryBlock = select.getQueryBlock();
            
            if (queryBlock != null) {
                // 提取源表
                List<LineageInfo.TableInfo> sourceTables = extractSourceTables(queryBlock);
                lineageInfo.setSourceTables(sourceTables);
                
                // 提取目标列
                List<LineageInfo.ColumnInfo> targetColumns = extractTargetColumns(queryBlock);
                lineageInfo.setTargetColumns(targetColumns);
                
                // 提取转换逻辑
                String transformation = extractTransformation(queryBlock);
                lineageInfo.setTransformation(transformation);
                
                lineageInfo.setStatementType("SELECT");
                lineageInfo.setRelationshipType(RelationshipType.DIRECT.getCode());
            }
            
        } catch (Exception e) {
            log.error("提取SELECT血缘失败", e);
        }
        
        return lineageInfo;
    }

    /**
     * 提取INSERT语句的血缘信息
     */
    private LineageInfo extractInsertLineage(SQLInsertStatement statement, String dbType) {
        LineageInfo lineageInfo = new LineageInfo();
        
        try {
            // 目标表
            String targetTable = statement.getTableName().getSimpleName();
            LineageInfo.TableInfo targetTableInfo = new LineageInfo.TableInfo();
            targetTableInfo.setTableName(targetTable);
            targetTableInfo.setNodeType(NodeType.TABLE.getCode());
            
            List<LineageInfo.TableInfo> targetTables = new ArrayList<>();
            targetTables.add(targetTableInfo);
            lineageInfo.setTargetTables(targetTables);
            
            // 目标列
            List<SQLExpr> columns = statement.getColumns();
            List<LineageInfo.ColumnInfo> targetColumns = new ArrayList<>();
            
            for (SQLExpr column : columns) {
                if (column instanceof SQLIdentifierExpr) {
                    LineageInfo.ColumnInfo columnInfo = new LineageInfo.ColumnInfo();
                    columnInfo.setColumnName(((SQLIdentifierExpr) column).getName());
                    columnInfo.setTableName(targetTable);
                    columnInfo.setNodeType(NodeType.COLUMN.getCode());
                    targetColumns.add(columnInfo);
                }
            }
            
            lineageInfo.setTargetColumns(targetColumns);
            
            // 源表（从子查询中提取）
            SQLSelect subQuery = statement.getQuery();
            if (subQuery != null) {
                SQLSelectQueryBlock queryBlock = subQuery.getQueryBlock();
                if (queryBlock != null) {
                    List<LineageInfo.TableInfo> sourceTables = extractSourceTables(queryBlock);
                    lineageInfo.setSourceTables(sourceTables);
                }
            }
            
            lineageInfo.setStatementType("INSERT");
            lineageInfo.setRelationshipType(RelationshipType.ETL.getCode());
            
        } catch (Exception e) {
            log.error("提取INSERT血缘失败", e);
        }
        
        return lineageInfo;
    }

    /**
     * 提取UPDATE语句的血缘信息
     */
    private LineageInfo extractUpdateLineage(SQLUpdateStatement statement, String dbType) {
        LineageInfo lineageInfo = new LineageInfo();
        
        try {
            // 目标表
            String targetTable = statement.getTableName().getSimpleName();
            LineageInfo.TableInfo targetTableInfo = new LineageInfo.TableInfo();
            targetTableInfo.setTableName(targetTable);
            targetTableInfo.setNodeType(NodeType.TABLE.getCode());
            
            List<LineageInfo.TableInfo> targetTables = new ArrayList<>();
            targetTables.add(targetTableInfo);
            lineageInfo.setTargetTables(targetTables);
            
            // 目标列
            List<SQLUpdateSetItem> items = statement.getItems();
            List<LineageInfo.ColumnInfo> targetColumns = new ArrayList<>();
            
            for (SQLUpdateSetItem item : items) {
                if (item.getColumn() instanceof SQLIdentifierExpr) {
                    LineageInfo.ColumnInfo columnInfo = new LineageInfo.ColumnInfo();
                    columnInfo.setColumnName(((SQLIdentifierExpr) item.getColumn()).getName());
                    columnInfo.setTableName(targetTable);
                    columnInfo.setNodeType(NodeType.COLUMN.getCode());
                    targetColumns.add(columnInfo);
                }
            }
            
            lineageInfo.setTargetColumns(targetColumns);
            
            lineageInfo.setStatementType("UPDATE");
            lineageInfo.setRelationshipType(RelationshipType.DIRECT.getCode());
            
        } catch (Exception e) {
            log.error("提取UPDATE血缘失败", e);
        }
        
        return lineageInfo;
    }

    /**
     * 提取CREATE VIEW语句的血缘信息
     */
    private LineageInfo extractViewLineage(SQLCreateViewStatement statement, String dbType) {
        LineageInfo lineageInfo = new LineageInfo();
        
        try {
            // 视图信息
            String viewName = statement.getName().getSimpleName();
            LineageInfo.TableInfo viewInfo = new LineageInfo.TableInfo();
            viewInfo.setTableName(viewName);
            viewInfo.setNodeType(NodeType.VIEW.getCode());
            
            List<LineageInfo.TableInfo> targetTables = new ArrayList<>();
            targetTables.add(viewInfo);
            lineageInfo.setTargetTables(targetTables);
            
            // 提取子查询
            SQLSelect subQuery = statement.getSubQuery();
            if (subQuery != null) {
                SQLSelectQueryBlock queryBlock = subQuery.getQueryBlock();
                if (queryBlock != null) {
                    List<LineageInfo.TableInfo> sourceTables = extractSourceTables(queryBlock);
                    lineageInfo.setSourceTables(sourceTables);
                    
                    List<LineageInfo.ColumnInfo> targetColumns = extractTargetColumns(queryBlock);
                    lineageInfo.setTargetColumns(targetColumns);
                    
                    String transformation = extractTransformation(queryBlock);
                    lineageInfo.setTransformation(transformation);
                }
            }
            
            lineageInfo.setStatementType("CREATE_VIEW");
            lineageInfo.setRelationshipType(RelationshipType.VIEW.getCode());
            
        } catch (Exception e) {
            log.error("提取VIEW血缘失败", e);
        }
        
        return lineageInfo;
    }

    /**
     * 提取源表信息
     */
    private List<LineageInfo.TableInfo> extractSourceTables(SQLSelectQueryBlock queryBlock) {
        List<LineageInfo.TableInfo> tables = new ArrayList<>();
        
        try {
            SQLTableSource from = queryBlock.getFrom();
            if (from != null) {
                extractTableSource(from, tables);
            }
        } catch (Exception e) {
            log.error("提取源表失败", e);
        }
        
        return tables;
    }

    /**
     * 递归提取表源
     */
    private void extractTableSource(SQLTableSource tableSource, List<LineageInfo.TableInfo> tables) {
        if (tableSource instanceof SQLExprTableSource) {
            // 基础表
            SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;
            String tableName = exprTableSource.getTableName();
            
            LineageInfo.TableInfo tableInfo = new LineageInfo.TableInfo();
            tableInfo.setTableName(tableName);
            tableInfo.setNodeType(NodeType.TABLE.getCode());
            tableInfo.setAlias(exprTableSource.getAlias());
            
            tables.add(tableInfo);
            
        } else if (tableSource instanceof SQLJoinTableSource) {
            // JOIN表
            SQLJoinTableSource joinTableSource = (SQLJoinTableSource) tableSource;
            
            // 递归处理左表
            SQLTableSource left = joinTableSource.getLeft();
            if (left != null) {
                extractTableSource(left, tables);
            }
            
            // 递归处理右表
            SQLTableSource right = joinTableSource.getRight();
            if (right != null) {
                extractTableSource(right, tables);
            }
            
        } else if (tableSource instanceof SQLSubqueryTableSource) {
            // 子查询
            SQLSubqueryTableSource subqueryTableSource = (SQLSubqueryTableSource) tableSource;
            SQLSelect subQuery = subqueryTableSource.getSelect();
            if (subQuery != null && subQuery.getQueryBlock() != null) {
                List<LineageInfo.TableInfo> subTables = extractSourceTables(subQuery.getQueryBlock());
                tables.addAll(subTables);
            }
        }
    }

    /**
     * 提取目标列信息
     */
    private List<LineageInfo.ColumnInfo> extractTargetColumns(SQLSelectQueryBlock queryBlock) {
        List<LineageInfo.ColumnInfo> columns = new ArrayList<>();
        
        try {
            List<SQLSelectItem> selectItems = queryBlock.getSelectList();
            
            for (SQLSelectItem item : selectItems) {
                LineageInfo.ColumnInfo columnInfo = new LineageInfo.ColumnInfo();
                
                // 提取列名
                String columnName = extractColumnName(item);
                if (columnName != null) {
                    columnInfo.setColumnName(columnName);
                    columnInfo.setNodeType(NodeType.COLUMN.getCode());
                    
                    // 提取表名
                    String tableName = extractTableName(item);
                    if (tableName != null) {
                        columnInfo.setTableName(tableName);
                    }
                    
                    // 提取表达式
                    String expression = extractExpression(item);
                    if (expression != null) {
                        columnInfo.setExpression(expression);
                    }
                    
                    columns.add(columnInfo);
                }
            }
        } catch (Exception e) {
            log.error("提取目标列失败", e);
        }
        
        return columns;
    }

    /**
     * 提取列名
     */
    private String extractColumnName(SQLSelectItem item) {
        if (item.getAlias() != null) {
            return item.getAlias();
        } else if (item.getExpr() instanceof SQLIdentifierExpr) {
            return ((SQLIdentifierExpr) item.getExpr()).getName();
        } else if (item.getExpr() instanceof SQLPropertyExpr) {
            return ((SQLPropertyExpr) item.getExpr()).getName();
        }
        return null;
    }

    /**
     * 提取表名
     */
    private String extractTableName(SQLSelectItem item) {
        if (item.getExpr() instanceof SQLPropertyExpr) {
            SQLExpr owner = ((SQLPropertyExpr) item.getExpr()).getOwner();
            if (owner instanceof SQLIdentifierExpr) {
                return ((SQLIdentifierExpr) owner).getName();
            }
        }
        return null;
    }

    /**
     * 提取表达式
     */
    private String extractExpression(SQLSelectItem item) {
        if (item.getExpr() != null) {
            return item.getExpr().toString();
        }
        return null;
    }

    /**
     * 提取转换逻辑
     */
    private String extractTransformation(SQLSelectQueryBlock queryBlock) {
        StringBuilder transformation = new StringBuilder();
        
        try {
            // WHERE条件
            SQLExpr where = queryBlock.getWhere();
            if (where != null) {
                transformation.append("WHERE: ").append(where.toString()).append("; ");
            }
            
            // GROUP BY
            SQLSelectGroupByClause groupBy = queryBlock.getGroupBy();
            if (groupBy != null) {
                transformation.append("GROUP BY: ").append(groupBy.toString()).append("; ");
            }
            
            // HAVING
            SQLExpr having = queryBlock.getHaving();
            if (having != null) {
                transformation.append("HAVING: ").append(having.toString()).append("; ");
            }
            
            // ORDER BY
            SQLOrderBy orderBy = queryBlock.getOrderBy();
            if (orderBy != null) {
                transformation.append("ORDER BY: ").append(orderBy.toString()).append("; ");
            }
            
        } catch (Exception e) {
            log.error("提取转换逻辑失败", e);
        }
        
        return transformation.length() > 0 ? transformation.toString() : null;
    }
}