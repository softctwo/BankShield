package com.bankshield.lineage.extractor.sql;

import com.bankshield.lineage.vo.LineageInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SqlLineageExtractorSimpleTest {

    @Test
    public void testExtractFromSqlBasic() {
        SqlLineageExtractor extractor = new SqlLineageExtractor();
        
        String sql = "SELECT * FROM users WHERE id = 1";
        String dbType = "mysql";
        
        LineageInfo result = extractor.extractFromSql(sql, dbType);
        
        assertNotNull(result);
        assertEquals("SELECT", result.getStatementType());
        assertNotNull(result.getSourceTables());
        assertFalse(result.getSourceTables().isEmpty());
        assertEquals("temp_table", result.getSourceTables().get(0).getTableName());
    }

    @Test
    public void testExtractFromSqlWithNullParams() {
        SqlLineageExtractor extractor = new SqlLineageExtractor();
        
        LineageInfo result = extractor.extractFromSql(null, null);
        assertNull(result);
    }

    @Test
    public void testExtractFromSqlWithEmptySql() {
        SqlLineageExtractor extractor = new SqlLineageExtractor();
        
        LineageInfo result = extractor.extractFromSql("", "mysql");
        assertNotNull(result);
        assertEquals("SELECT", result.getStatementType());
    }
}