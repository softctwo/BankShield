package com.bankshield.lineage.extractor.sql;

import com.bankshield.lineage.vo.LineageInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SqlLineageExtractorTest {

    @Autowired
    private SqlLineageExtractor sqlLineageExtractor;

    @Test
    public void testExtractFromSql() {
        String sql = "SELECT * FROM users WHERE id = 1";
        String dbType = "mysql";
        
        LineageInfo result = sqlLineageExtractor.extractFromSql(sql, dbType);
        
        assertNotNull(result);
        assertEquals("SELECT", result.getStatementType());
        assertNotNull(result.getSourceTables());
        assertFalse(result.getSourceTables().isEmpty());
    }

    @Test
    public void testExtractFromSqlWithNullParams() {
        LineageInfo result = sqlLineageExtractor.extractFromSql(null, null);
        assertNull(result);
    }

    @Test
    public void testExtractFromSqlWithEmptySql() {
        LineageInfo result = sqlLineageExtractor.extractFromSql("", "mysql");
        assertNotNull(result);
    }
}