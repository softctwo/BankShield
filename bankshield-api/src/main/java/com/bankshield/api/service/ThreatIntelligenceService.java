package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.ThreatIntelligence;
import com.bankshield.api.mapper.ThreatIntelligenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 威胁情报服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreatIntelligenceService {

    private final ThreatIntelligenceMapper threatIntelligenceMapper;

    /**
     * 分页查询威胁情报
     */
    public IPage<ThreatIntelligence> getThreatIntelligencePage(Page<ThreatIntelligence> page, 
                                                                String threatType, String threatLevel,
                                                                String status, LocalDateTime startTime, 
                                                                LocalDateTime endTime) {
        return threatIntelligenceMapper.selectThreatIntelligencePage(page, threatType, threatLevel,
                                                                   status, startTime, endTime);
    }

    /**
     * 获取活跃威胁情报
     */
    public List<ThreatIntelligence> getActiveThreats(int limit) {
        return threatIntelligenceMapper.selectActiveThreats(limit);
    }

    /**
     * 根据ID获取威胁情报
     */
    public ThreatIntelligence getThreatById(Long id) {
        return threatIntelligenceMapper.selectById(id);
    }

    /**
     * 保存威胁情报
     */
    @Transactional
    public boolean saveThreatIntelligence(ThreatIntelligence threat) {
        if (threat.getId() == null) {
            threat.setDiscoverTime(LocalDateTime.now());
            threat.setStatus("ACTIVE");
            return threatIntelligenceMapper.insert(threat) > 0;
        } else {
            return threatIntelligenceMapper.updateById(threat) > 0;
        }
    }

    /**
     * 更新威胁情报状态
     */
    @Transactional
    public boolean updateThreatStatus(Long id, String status) {
        ThreatIntelligence threat = threatIntelligenceMapper.selectById(id);
        if (threat != null) {
            threat.setStatus(status);
            return threatIntelligenceMapper.updateById(threat) > 0;
        }
        return false;
    }

    /**
     * 删除威胁情报
     */
    @Transactional
    public boolean deleteThreatIntelligence(Long id) {
        return threatIntelligenceMapper.deleteById(id) > 0;
    }

    /**
     * 根据IoC指标查询威胁
     */
    public List<ThreatIntelligence> getThreatsByIoc(String ioc) {
        return threatIntelligenceMapper.selectByIoc(ioc);
    }

    /**
     * 获取威胁统计信息
     */
    public Map<String, Object> getThreatStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 活跃威胁数量
            int activeCount = (int) (long) threatIntelligenceMapper.selectCount(
                new QueryWrapper<ThreatIntelligence>().eq("status", "ACTIVE")
            );
            stats.put("activeCount", activeCount);
            
            // 今日新增威胁
            int todayCount = threatIntelligenceMapper.countTodayThreats();
            stats.put("todayCount", todayCount);
            
            // 按威胁类型统计
            List<Map<String, Object>> typeStats = threatIntelligenceMapper.countByThreatType();
            stats.put("typeDistribution", typeStats);
            
            // 按威胁等级统计
            List<Map<String, Object>> levelStats = threatIntelligenceMapper.countByThreatLevel();
            stats.put("levelDistribution", levelStats);
            
            // 7天趋势
            List<Map<String, Object>> trend = threatIntelligenceMapper.get7DayTrend();
            stats.put("weekTrend", trend);
            
            // 地理位置分布
            List<Map<String, Object>> geoDistribution = threatIntelligenceMapper.getGeoDistribution();
            stats.put("geoDistribution", geoDistribution);
            
            // 目标行业分布
            List<Map<String, Object>> industryDistribution = threatIntelligenceMapper.getIndustryDistribution();
            stats.put("industryDistribution", industryDistribution);
            
            log.info("威胁情报统计获取成功");
        } catch (Exception e) {
            log.error("获取威胁情报统计失败", e);
            throw new RuntimeException("获取威胁情报统计失败", e);
        }
        
        return stats;
    }

    /**
     * 模拟获取外部威胁情报（定时任务）
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    @Transactional
    public void fetchExternalThreatIntelligence() {
        try {
            log.info("开始获取外部威胁情报...");
            
            // 模拟从外部源获取威胁情报数据
            List<ThreatIntelligence> externalThreats = simulateExternalThreats();
            
            int newThreatCount = 0;
            for (ThreatIntelligence threat : externalThreats) {
                // 检查是否已存在相同的威胁
                boolean exists = checkThreatExists(threat);
                if (!exists) {
                    threat.setDiscoverTime(LocalDateTime.now());
                    threat.setStatus("ACTIVE");
                    threatIntelligenceMapper.insert(threat);
                    newThreatCount++;
                }
            }
            
            log.info("外部威胁情报获取完成，新增 {} 条威胁", newThreatCount);
        } catch (Exception e) {
            log.error("获取外部威胁情报失败", e);
        }
    }

    /**
     * 模拟外部威胁数据
     */
    private List<ThreatIntelligence> simulateExternalThreats() {
        List<ThreatIntelligence> threats = new ArrayList<>();
        String[] threatTypes = {"MALWARE", "PHISHING", "DDOS", "INTRUSION", "DATA_LEAK"};
        String[] threatLevels = {"CRITICAL", "HIGH", "MEDIUM", "LOW"};
        String[] locations = {"北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "西安"};
        String[] industries = {"金融", "电信", "政府", "医疗", "教育", "能源"};
        
        Random random = new Random();
        
        for (int i = 0; i < 3; i++) {
            ThreatIntelligence threat = new ThreatIntelligence();
            threat.setThreatType(threatTypes[random.nextInt(threatTypes.length)]);
            threat.setThreatLevel(threatLevels[random.nextInt(threatLevels.length)]);
            threat.setThreatDescription(generateThreatDescription(threat.getThreatType()));
            threat.setThreatSource("外部威胁情报源");
            threat.setImpactScope("全国范围");
            threat.setIocIndicators(generateIocIndicators());
            threat.setConfidenceLevel("HIGH");
            threat.setGeoLocation(locations[random.nextInt(locations.length)]);
            threat.setTargetIndustry(industries[random.nextInt(industries.length)]);
            threat.setAttackTechniques(generateAttackTechniques());
            threat.setRecommendations(generateRecommendations(threat.getThreatType()));
            threat.setTags(generateTags(threat.getThreatType()));
            
            threats.add(threat);
        }
        
        return threats;
    }

    /**
     * 生成威胁描述
     */
    private String generateThreatDescription(String threatType) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("MALWARE", "发现新型恶意软件变种，具有高度隐蔽性和破坏性");
        descriptions.put("PHISHING", "检测到大规模钓鱼攻击活动，针对金融机构用户");
        descriptions.put("DDOS", "监测到分布式拒绝服务攻击，攻击流量持续增加");
        descriptions.put("INTRUSION", "发现网络入侵行为，攻击者正在尝试横向移动");
        descriptions.put("DATA_LEAK", "检测到数据泄露事件，敏感信息可能已外泄");
        
        return descriptions.getOrDefault(threatType, "发现未知威胁活动");
    }

    /**
     * 生成IoC指标
     */
    private String generateIocIndicators() {
        String[] iocs = {
            "md5: " + generateRandomHash(),
            "sha256: " + generateRandomHash(),
            "ip: " + generateRandomIp(),
            "domain: " + generateRandomDomain(),
            "url: " + generateRandomUrl()
        };
        
        return String.join(", ", iocs);
    }

    /**
     * 生成攻击技术
     */
    private String generateAttackTechniques() {
        String[] techniques = {
            "T1566.001 - Spearphishing Attachment",
            "T1059.003 - Windows Command Shell",
            "T1053.005 - Scheduled Task",
            "T1083 - File and Directory Discovery",
            "T1055 - Process Injection"
        };
        
        return String.join(", ", techniques);
    }

    /**
     * 生成建议
     */
    private String generateRecommendations(String threatType) {
        Map<String, String> recommendations = new HashMap<>();
        recommendations.put("MALWARE", "立即更新杀毒软件，隔离受感染系统，进行全盘扫描");
        recommendations.put("PHISHING", "加强用户安全意识培训，启用邮件过滤和双因子认证");
        recommendations.put("DDOS", "启用DDoS防护服务，增加带宽容量，配置流量清洗");
        recommendations.put("INTRUSION", "立即隔离受影响的网络段，检查系统日志，修补安全漏洞");
        recommendations.put("DATA_LEAK", "立即更改泄露的凭据，通知受影响的用户，加强数据加密");
        
        return recommendations.getOrDefault(threatType, "加强安全监控，及时更新安全策略");
    }

    /**
     * 生成标签
     */
    private String generateTags(String threatType) {
        Map<String, String> tags = new HashMap<>();
        tags.put("MALWARE", "恶意软件,病毒,安全威胁");
        tags.put("PHISHING", "钓鱼,社会工程,邮件安全");
        tags.put("DDOS", "拒绝服务,网络攻击,可用性");
        tags.put("INTRUSION", "入侵,横向移动,渗透");
        tags.put("DATA_LEAK", "数据泄露,隐私,合规");
        
        return tags.getOrDefault(threatType, "安全威胁,网络安全");
    }

    /**
     * 生成随机哈希值
     */
    private String generateRandomHash() {
        String chars = "0123456789abcdef";
        StringBuilder hash = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 32; i++) {
            hash.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return hash.toString();
    }

    /**
     * 生成随机IP地址
     */
    private String generateRandomIp() {
        Random random = new Random();
        return String.format("%d.%d.%d.%d",
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256));
    }

    /**
     * 生成随机域名
     */
    private String generateRandomDomain() {
        String[] domains = {"malware.com", "phishing.net", "attack.org", "threat.info", "danger.co"};
        return domains[new Random().nextInt(domains.length)];
    }

    /**
     * 生成随机URL
     */
    private String generateRandomUrl() {
        return "http://" + generateRandomDomain() + "/malicious/path";
    }

    /**
     * 检查威胁是否已存在
     */
    private boolean checkThreatExists(ThreatIntelligence threat) {
        List<ThreatIntelligence> existing = threatIntelligenceMapper.selectList(
            new QueryWrapper<ThreatIntelligence>()
                .eq("threat_type", threat.getThreatType())
                .eq("threat_level", threat.getThreatLevel())
                .eq("threat_description", threat.getThreatDescription())
                .ge("discover_time", LocalDateTime.now().minusHours(24)) // 24小时内
        );
        
        return !existing.isEmpty();
    }

    /**
     * 清理过期威胁情报
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    @Transactional
    public void cleanupExpiredThreats() {
        try {
            log.info("开始清理过期威胁情报...");
            
            LocalDateTime expiryTime = LocalDateTime.now().minusDays(30); // 30天前的数据
            
            int deletedCount = threatIntelligenceMapper.delete(
                new QueryWrapper<ThreatIntelligence>()
                    .lt("discover_time", expiryTime)
                    .or()
                    .eq("status", "EXPIRED")
            );
            
            log.info("威胁情报清理完成，删除 {} 条过期数据", deletedCount);
        } catch (Exception e) {
            log.error("清理过期威胁情报失败", e);
        }
    }
}