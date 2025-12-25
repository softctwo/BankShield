package com.bankshield.gateway;

import com.bankshield.gateway.entity.BlacklistIp;
import com.bankshield.gateway.service.BlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 黑名单服务测试类
 * 
 * @author BankShield
 */
@SpringBootTest
public class BlacklistServiceTest {
    
    @Autowired
    private BlacklistService blacklistService;
    
    @Test
    public void testAddToBlacklist() {
        BlacklistIp blacklistIp = blacklistService.addToBlacklist(
            "192.168.1.100", 
            "测试封禁", 
            3600L, 
            "test"
        );
        
        assertNotNull(blacklistIp.getId());
        assertEquals("192.168.1.100", blacklistIp.getIpAddress());
        assertEquals("测试封禁", blacklistIp.getBlockReason());
        assertEquals("BLOCKED", blacklistIp.getBlockStatus());
    }
    
    @Test
    public void testIsBlacklisted() {
        String testIp = "192.168.1.101";
        
        // 先添加到黑名单
        blacklistService.addToBlacklist(testIp, "测试封禁", 3600L, "test");
        
        // 检查是否在黑名单中
        boolean isBlacklisted = blacklistService.isBlacklisted(testIp);
        assertTrue(isBlacklisted);
        
        // 检查不在黑名单中的IP
        isBlacklisted = blacklistService.isBlacklisted("192.168.1.1");
        assertFalse(isBlacklisted);
    }
    
    @Test
    public void testGetStatistics() {
        var statistics = blacklistService.getStatistics();
        assertNotNull(statistics);
        assertTrue(statistics.getTotalCount() >= 0);
    }
}