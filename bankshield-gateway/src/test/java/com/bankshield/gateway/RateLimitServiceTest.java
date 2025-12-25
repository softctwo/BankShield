package com.bankshield.gateway;

import com.bankshield.gateway.entity.RateLimitRule;
import com.bankshield.gateway.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 限流服务测试类
 * 
 * @author BankShield
 */
@SpringBootTest
public class RateLimitServiceTest {
    
    @Autowired
    private RateLimitService rateLimitService;
    
    @Test
    public void testCreateRule() {
        RateLimitRule rule = new RateLimitRule();
        rule.setRuleName("test-rule");
        rule.setLimitDimension("IP");
        rule.setLimitThreshold(100);
        rule.setLimitWindow(1);
        rule.setEnabled(true);
        rule.setDescription("测试规则");
        
        RateLimitRule created = rateLimitService.createRule(rule);
        assertNotNull(created.getId());
        assertEquals("test-rule", created.getRuleName());
    }
    
    @Test
    public void testIsAllowed() {
        // 测试默认规则
        boolean allowed = rateLimitService.isAllowed("IP", "127.0.0.1");
        assertTrue(allowed);
        
        // 测试自定义规则
        allowed = rateLimitService.isAllowed("IP", "127.0.0.1", "test-rule");
        assertTrue(allowed);
    }
    
    @Test
    public void testGetRuleByName() {
        var rule = rateLimitService.getRuleByName("IP默认限流");
        assertTrue(rule.isPresent());
        assertEquals("IP默认限流", rule.get().getRuleName());
    }
}