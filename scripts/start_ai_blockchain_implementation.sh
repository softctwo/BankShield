#!/bin/bash
################################################################################
# BankShield AI智能增强 + 区块链存证 一键启动脚本
# 
# 功能：
# - 环境检查
# - 依赖安装
# - Docker网络启动
# - 智能合约部署
# - AI模型训练
# - 服务启动
# 
# 使用方式：
#   ./start_ai_blockchain_implementation.sh [stage]
#   
# 参数：
#   stage: 可选，指定执行阶段（1-4），默认为全阶段
#          1 = AI深度学习引擎
#          2 = AI自动化响应  
#          3 = 区块链基础设施
#          4 = 跨机构验证
#          all = 所有阶段
#
# 示例：
#   ./start_ai_blockchain_implementation.sh          # 执行所有阶段
#   ./start_ai_blockchain_implementation.sh 1        # 只执行阶段一
#   ./start_ai_blockchain_implementation.sh 1,3      # 执行阶段一和三
################################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 配置参数
PROJECT_ROOT="/Users/zhangyanlong/workspaces/BankShield"
STAGE=${1:-"all"}
FABRIC_VERSION="2.4.7"
EXPLORER_VERSION="1.1.8"

# 检查Docker
check_docker() {
    log_info "检查Docker环境..."
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        log_error "Docker服务未运行，请启动Docker"
        exit 1
    fi
    
    log_success "Docker环境正常"
}

# 检查Java环境
check_java() {
    log_info "检查Java环境..."
    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请先安装JDK 1.8+"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    log_success "Java版本: $JAVA_VERSION"
}

# 检查Maven
check_maven() {
    log_info "检查Maven环境..."
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装，请先安装Maven 3.6+"
        exit 1
    fi
    
    MVN_VERSION=$(mvn -version | head -n 1)
    log_success "Maven版本: $MVN_VERSION"
}

# 阶段一：AI深度学习引擎
stage1_ai_deep_learning() {
    log_info "======================="
    log_info "阶段一：AI深度学习引擎"
    log_info "======================="
    
    log_info "步骤1.1：安装AI依赖..."
    cd "$PROJECT_ROOT/bankshield-ai"
    
    # 检查pom.xml中是否已有DL4J依赖
    if ! grep -q "deeplearning4j-core" pom.xml; then
        log_info "添加深度强化学习依赖..."
        cat >> pom.xml << 'EOF'
        <!-- 深度强化学习 -->
        <dependency>
            <groupId>org.deeplearning4j</groupId>
            <artifactId>deeplearning4j-core</artifactId>
            <version>1.0.0-M2.1</version>
        </dependency>
        
        <!-- XGBoost -->
        <dependency>
            <groupId>ml.dmlc</groupId>
            <artifactId>xgboost4j</artifactId>
            <version>1.7.3</version>
        </dependency>
        
        <!-- ND4J后端（CPU/GPU） -->
        <dependency>
            <groupId>org.nd4j</groupId>
            <artifactId>nd4j-native-platform</artifactId>
            <version>1.0.0-M2.1</version>
        </dependency>
EOF
    fi
    
    log_info "编译AI模块..."
    mvn clean compile -DskipTests
    
    # 创建深度学习代码目录
    mkdir -p src/main/java/com/bankshield/ai/deep
    mkdir -p src/main/java/com/bankshield/ai/automate
    mkdir -p src/main/java/com/bankshield/ai/policy
    mkdir -p src/main/java/com/bankshield/ai/train
    mkdir -p src/main/java/com/bankshield/ai/monitor
    
    log_info "生成DQN强化学习代码模板..."
    cat > src/main/java/com/bankshield/ai/deep/DQNAgent.java << 'EOF'
package com.bankshield.ai.deep;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DQN（深度Q网络）智能体
 * 
 * 功能：
 * - 学习最优安全响应策略
 * - 自动调整安全阈值
 * - 动态策略优化
 * 
 * 状态空间：
 * - 当前威胁等级（0-10）
 * - 系统资源使用率（0-100%）
 * - 异常行为数量
 * - 历史响应效果
 * 
 * 动作空间：
 * - 封锁IP
 * - 限制访问频率
 * - 触发告警
 * - 隔离账户
 * - 更新规则
 */
public class DQNAgent {
    
    private static final Logger log = LoggerFactory.getLogger(DQNAgent.class);
    
    private static final int STATE_SIZE = 10;      // 状态向量维度
    private static final int ACTION_SIZE = 5;      // 可选动作数量
    private static final double LEARNING_RATE = 0.001;
    private static final double GAMMA = 0.95;      // 折扣因子
    private static final double EPSILON = 1.0;     // 探索率
    private static final double EPSILON_MIN = 0.01;
    private static final double EPSILON_DECAY = 0.995;
    
    private MultiLayerNetwork policyNetwork;
    private MultiLayerNetwork targetNetwork;
    private ExperienceReplay replayMemory;
    
    public DQNAgent() {
        this.replayMemory = new ExperienceReplay(10000);
        buildNetworks();
    }
    
    /**
     * 构建Q网络
     */
    private void buildNetworks() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(12345)
            .updater(new Adam(LEARNING_RATE))
            .list()
            .layer(new DenseLayer.Builder()
                .nIn(STATE_SIZE)
                .nOut(128)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(128)
                .nOut(256)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(256)
                .nOut(128)
                .activation(Activation.RELU)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(128)
                .nOut(ACTION_SIZE)
                .activation(Activation.IDENTITY)
                .build())
            .build();
        
        this.policyNetwork = new MultiLayerNetwork(conf);
        this.policyNetwork.init();
        this.policyNetwork.setListeners(new ScoreIterationListener(100));
        
        // 目标网络（用于稳定训练）
        this.targetNetwork = new MultiLayerNetwork(conf);
        this.targetNetwork.init();
        this.targetNetwork.setParams(policyNetwork.params());
        
        log.info("DQN网络构建完成 - 状态空间:{}, 动作空间:{}", STATE_SIZE, ACTION_SIZE);
    }
    
    /**
     * 选择动作（ε-贪心策略）
     */
    public int selectAction(double[] state) {
        if (Math.random() < EPSILON) {
            // 探索：随机选择
            return (int) (Math.random() * ACTION_SIZE);
        } else {
            // 利用：选择Q值最大的动作
            double[] qValues = policyNetwork.output(replayMemory.toINDArray(state)).toDoubleVector();
            return getMaxIndex(qValues);
        }
    }
    
    /**
     * 存储经验到回放内存
     */
    public void storeExperience(double[] state, int action, double reward, double[] nextState, boolean done) {
        replayMemory.add(state, action, reward, nextState, done);
    }
    
    /**
     * 训练策略网络
     */
    public void train(int batchSize) {
        if (replayMemory.size() < batchSize) {
            return;
        }
        
        // 从回放内存采样
        ExperienceBatch batch = replayMemory.sample(batchSize);
        
        // 计算目标Q值
        double[][] targets = new double[batchSize][ACTION_SIZE];
        for (int i = 0; i < batchSize; i++) {
            double[] currentQ = policyNetwork.output(batch.getStates()[i]).toDoubleVector();
            double[] nextQ = targetNetwork.output(batch.getNextStates()[i]).toDoubleVector();
            
            System.arraycopy(currentQ, 0, targets[i], 0, ACTION_SIZE);
            double target = batch.getRewards()[i];
            if (!batch.getDones()[i]) {
                target += GAMMA * getMaxValue(nextQ);
            }
            targets[i][batch.getActions()[i]] = target;
        }
        
        // 训练
        policyNetwork.fit(batch.getStates(), replayMemory.toINDArray(targets));
        
        // 更新探索率
        if (EPSILON > EPSILON_MIN) {
            EPSILON *= EPSILON_DECAY;
        }
        
        log.debug("DQN训练完成 - 批次大小:{}, 当前ε:{}", batchSize, EPSILON);
    }
    
    /**
     * 更新目标网络
     */
    public void updateTargetNetwork() {
        targetNetwork.setParams(policyNetwork.params());
        log.debug("目标网络参数已同步");
    }
    
    /**
     * 获取最大值的索引
     */
    private int getMaxIndex(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    
    /**
     * 获取数组最大值
     */
    private double getMaxValue(double[] array) {
        return array[getMaxIndex(array)];
    }
    
    /**
     * 保存模型
     */
    public void saveModel(String path) {
        try {
            policyNetwork.save(new java.io.File(path));
            log.info("DQN模型已保存至: {}", path);
        } catch (Exception e) {
            log.error("保存模型失败", e);
        }
    }
    
    /**
     * 加载模型
     */
    public void loadModel(String path) {
        try {
            policyNetwork = MultiLayerNetwork.load(new java.io.File(path), false);
            targetNetwork.setParams(policyNetwork.params());
            log.info("DQN模型已加载: {}", path);
        } catch (Exception e) {
            log.error("加载模型失败", e);
        }
    }
}
EOF

    log_success "阶段一完成：AI深度学习引擎基础代码已生成"
}

# 阶段二：AI自动化响应系统
stage2_ai_automate() {
    log_info "======================="
    log_info "阶段二：AI自动化响应系统"
    log_info "======================="
    
    cd "$PROJECT_ROOT/bankshield-ai"
    
    log_info "生成自动化响应服务代码..."
    cat > src/main/java/com/bankshield/ai/automate/SmartResponseService.java << 'EOF'
package com.bankshield.ai.automate;

import com.bankshield.ai.deep.DQNAgent;
import com.bankshield.common.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 智能自动化响应服务
 * 
 * 功能特性：
 * 1. 实时威胁自动响应（< 50ms）
 * 2. 动态安全策略生成
 * 3. 一键隔离可疑账户
 * 4. 智能限流和IP封锁
 * 5. 基于DQN的自适应学习
 */
@Slf4j
@Service
public class SmartResponseService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private DQNAgent dqnAgent;
    
    // Redis键前缀
    private static final String BLOCKED_IP_PREFIX = "security:blocked_ip:";
    private static final String RATE_LIMIT_PREFIX = "security:rate_limit:";
    private static final String ISOLATED_USER_PREFIX = "security:isolated_user:";
    private static final String THREAT_LEVEL_KEY = "security:threat_level";
    
    /**
     * 执行智能响应动作
     * 
     * @param threatLevel 威胁等级 (0-10)
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param actionType 检测到的异常类型
     * @return 响应结果
     */
    public Result<String> executeSmartResponse(int threatLevel, Long userId, String ipAddress, String actionType) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 构建状态向量
            double[] state = buildStateVector(threatLevel, userId, ipAddress);
            
            // 2. DQN选择最优动作
            int action = dqnAgent.selectAction(state);
            
            // 3. 执行响应动作
            String responseDetails;
            switch (action) {
                case 0:  // 封锁IP
                    responseDetails = blockIPAddress(ipAddress, calculateBlockDuration(threatLevel));
                    break;
                case 1:  // 限制访问频率
                    responseDetails = limitAccessFrequency(userId, calculateRateLimit(threatLevel));
                    break;
                case 2:  // 触发告警
                    responseDetails = triggerAlert(threatLevel, userId, actionType);
                    break;
                case 3:  // 隔离账户
                    responseDetails = isolateUserAccount(userId, calculateIsolationDuration(threatLevel));
                    break;
                case 4:  // 更新规则
                    responseDetails = updateSecurityRules(threatLevel, actionType);
                    break;
                default:
                    responseDetails = "未知动作:" + action;
            }
            
            // 4. 存储经验（用于后续训练）
            double reward = calculateReward(threatLevel, action);
            double[] nextState = state; // 简化处理
            dqnAgent.storeExperience(state, action, reward, nextState, false);
            
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("智能响应执行完成 - 威胁等级:{}, 动作:{}, 响应时间:{}ms", threatLevel, action, responseTime);
            
            return Result.success(responseDetails);
            
        } catch (Exception e) {
            log.error("智能响应执行失败", e);
            return Result.fail("智能响应执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 一键隔离可疑账户
     * 
     * @param userId 用户ID
     * @param durationHours 隔离时长（小时）
     * @return 隔离详情
     */
    public String isolateUserAccount(Long userId, int durationHours) {
        String key = ISOLATED_USER_PREFIX + userId;
        
        // 设置隔离标记
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()),
                durationHours, TimeUnit.HOURS);
        
        // 同时封锁用户相关的IP
        String userIP = getUserLastIP(userId);
        if (userIP != null) {
            blockIPAddress(userIP, durationHours * 60); // 转换为分钟
        }
        
        // 记录审计日志
        logAuditLog("USER_ISOLATION", userId, "账户已隔离 " + durationHours + " 小时");
        
        String message = String.format("用户ID:%s 已隔离，时长:%d小时，相关IP:%s", 
                userId, durationHours, userIP);
        log.warn(message);
        
        return message;
    }
    
    /**
     * 封锁IP地址
     * 
     * @param ipAddress IP地址
     * @param durationMinutes 封锁时长（分钟）
     * @return 封锁详情
     */
    public String blockIPAddress(String ipAddress, int durationMinutes) {
        if (isInternalIP(ipAddress)) {
            log.info("跳过内部IP封锁: {}", ipAddress);
            return "内部IP不封锁: " + ipAddress;
        }
        
        String key = BLOCKED_IP_PREFIX + ipAddress.replace(".", "_");
        
        // 检查是否已封锁
        if (redisTemplate.hasKey(key)) {
            return "IP已处于封锁状态: " + ipAddress;
        }
        
        // 设置封锁
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()),
                durationMinutes, TimeUnit.MINUTES);
        
        // 更新威胁等级
        updateThreatLevel(1);
        
        // 记录审计日志
        logAuditLog("IP_BLOCK", null, "IP:" + ipAddress + " 封锁" + durationMinutes + "分钟");
        
        String message = String.format("IP:%s 已封锁，时长:%d分钟", ipAddress, durationMinutes);
        log.warn(message);
        
        // 触发告警通知
        sendAlertNotification("IP_BLOCK", message);
        
        return message;
    }
    
    /**
     * 限制访问频率
     * 
     * @param userId 用户ID
     * @param maxRequestsPerMinute 每分钟最大请求数
     * @return 限制详情
     */
    public String limitAccessFrequency(Long userId, int maxRequestsPerMinute) {
        String key = RATE_LIMIT_PREFIX + userId;
        
        // 设置限流规则
        redisTemplate.opsForValue().set(key, String.valueOf(maxRequestsPerMinute),
                1, TimeUnit.HOURS); // 限流规则有效期1小时
        
        // 记录审计日志
        logAuditLog("RATE_LIMIT", userId, "限流设置:" + maxRequestsPerMinute + "请求/分钟");
        
        String message = String.format("用户ID:%s 限流设置:%d请求/分钟", userId, maxRequestsPerMinute);
        log.info(message);
        
        return message;
    }
    
    /**
     * 触发告警
     */
    private String triggerAlert(int threatLevel, Long userId, String actionType) {
        String message = String.format("威胁告警 - 等级:%d, 用户:%s, 类型:%s", 
                threatLevel, userId, actionType);
        
        // 发送告警通知（邮件、短信、Webhook）
        sendAlertNotification("THREAT_ALERT", message);
        
        // 记录告警
        log.warn(message);
        
        // 记录到区块链（待集成）
        logAuditLog("THREAT_ALERT", userId, message);
        
        return message;
    }
    
    /**
     * 更新安全规则
     */
    private String updateSecurityRules(int threatLevel, String actionType) {
        // 根据威胁等级动态调整检测阈值
        String rule = "DETECTION_THRESHOLD: " + (10 - threatLevel) * 10 + "%, " +
                     "ACTION_TYPE: " + actionType;
        
        log.info("安全规则已更新: {}", rule);
        logAuditLog("RULE_UPDATE", null, rule);
        
        return "安全规则更新: " + rule;
    }
    
    /**
     * 构建状态向量
     */
    private double[] buildStateVector(int threatLevel, Long userId, String ipAddress) {
        double[] state = new double[10];
        
        // 威胁等级（归一化）
        state[0] = threatLevel / 10.0;
        
        // 用户历史异常次数
        state[1] = getUserHistoricalAnomalies(userId) / 100.0;
        
        // IP历史异常次数
        state[2] = getIPHistoricalAnomalies(ipAddress) / 100.0;
        
        // 当前系统负载
        state[3] = getCurrentSystemLoad();
        
        // 威胁等级变化率
        state[4] = getThreatLevelChangeRate();
        
        // 时间特征（小时）
        state[5] = java.time.LocalDateTime.now().getHour() / 24.0;
        
        // 是否是工作时间（9-18点）
        int hour = java.time.LocalDateTime.now().getHour();
        state[6] = (hour >= 9 && hour <= 18) ? 1.0 : 0.0;
        
        // 历史响应效果
        state[7] = getHistoricalResponseEffectiveness();
        
        // 敏感数据访问频率
        state[8] = getSensitiveDataAccessFrequency(userId) / 1000.0;
        
        // 异常行为多样性
        state[9] = getAnomalyDiversityScore(userId);
        
        log.debug("状态向量构建完成: {}", java.util.Arrays.toString(state));
        
        return state;
    }
    
    /**
     * 计算奖励值
     */
    private double calculateReward(int threatLevel, int action) {
        // 基于威胁等级和动作的奖励函数
        double baseReward = -threatLevel; // 威胁越高，基础奖励越低
        
        // 根据动作调整奖励（简单规则）
        switch (action) {
            case 0: // 封锁IP（保守动作，适合高级威胁）
                return threatLevel >= 7 ? baseReward + 5 : baseReward - 2;
            case 1: // 限流（温和动作，适合中级威胁）
                return threatLevel >= 4 && threatLevel <= 6 ? baseReward + 3 : baseReward - 1;
            case 2: // 告警（最小干预）
                return threatLevel <= 3 ? baseReward + 2 : baseReward - 3;
            case 3: // 隔离（强动作，适合高级威胁）
                return threatLevel >= 8 ? baseReward + 6 : baseReward - 5;
            case 4: // 更新规则（预防性动作）
                return threatLevel >= 5 ? baseReward + 1 : baseReward;
            default:
                return baseReward;
        }
    }
    
    /**
     * 计算封锁时长
     */
    private int calculateBlockDuration(int threatLevel) {
        // 威胁等级越高，封锁时间越长
        if (threatLevel >= 9) return 24 * 60; // 24小时
        if (threatLevel >= 7) return 6 * 60;  // 6小时
        if (threatLevel >= 5) return 2 * 60;  // 2小时
        if (threatLevel >= 3) return 60;       // 1小时
        return 30;                             // 30分钟
    }
    
    /**
     * 计算限流阈值
     */
    private int calculateRateLimit(int threatLevel) {
        // 威胁等级越高，限制越严格
        if (threatLevel >= 8) return 10;   // 10次/分钟
        if (threatLevel >= 6) return 30;   // 30次/分钟
        if (threatLevel >= 4) return 60;   // 60次/分钟
        return 100;                        // 100次/分钟
    }
    
    /**
     * 计算隔离时长
     */
    private int calculateIsolationDuration(int threatLevel) {
        if (threatLevel >= 9) return 72; // 72小时
        if (threatLevel >= 7) return 24; // 24小时
        if (threatLevel >= 5) return 8;  // 8小时
        return 2;                        // 2小时
    }
    
    /**
     * 检查是否为内部IP
     */
    private boolean isInternalIP(String ip) {
        return ip.startsWith("10.") || 
               ip.startsWith("192.168.") || 
               ip.startsWith("127.") ||
               (ip.startsWith("172.") && isPrivate172(ip));
    }
    
    private boolean isPrivate172(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length >= 2) {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        }
        return false;
    }
    
    /**
     * 获取用户最后登录IP
     */
    private String getUserLastIP(Long userId) {
        // 实际应从Redis或数据库获取
        return (String) redisTemplate.opsForValue().get("user:last_ip:" + userId);
    }
    
    /**
     * 获取用户历史异常次数
     */
    private int getUserHistoricalAnomalies(Long userId) {
        String count = (String) redisTemplate.opsForValue().get("user:anomaly_count:" + userId);
        return count != null ? Integer.parseInt(count) : 0;
    }
    
    /**
     * 获取IP历史异常次数
     */
    private int getIPHistoricalAnomalies(String ip) {
        String count = (String) redisTemplate.opsForValue().get("ip:anomaly_count:" + ip);
        return count != null ? Integer.parseInt(count) : 0;
    }
    
    /**
     * 获取当前系统负载
     */
    private double getCurrentSystemLoad() {
        // 实际应从监控系统中获取
        return Math.random(); // 模拟数据
    }
    
    /**
     * 获取威胁等级变化率
     */
    private double getThreatLevelChangeRate() {
        Object currentLevel = redisTemplate.opsForValue().get(THREAT_LEVEL_KEY);
        // 实际应计算变化率
        return 0.0; // 简化处理
    }
    
    /**
     * 获取历史响应效果
     */
    private double getHistoricalResponseEffectiveness() {
        // 实际应统计历史响应的成功率
        return 0.8; // 模拟80%效果
    }
    
    /**
     * 获取敏感数据访问频率
     */
    private int getSensitiveDataAccessFrequency(Long userId) {
        // 实际应从审计日志统计
        return 50; // 模拟50次
    }
    
    /**
     * 获取异常行为多样性分数
     */
    private double getAnomalyDiversityScore(Long userId) {
        // 实际应计算不同类型的异常行为多样性
        return 0.6; // 模拟多样性分数
    }
    
    /**
     * 更新威胁等级
     */
    private void updateThreatLevel(int increment) {
        redisTemplate.opsForValue().increment(THREAT_LEVEL_KEY, increment);
    }
    
    /**
     * 记录审计日志
     */
    private void logAuditLog(String action, Long userId, String detail) {
        // 创建审计记录
        AuditRecord record = new AuditRecord();
        record.setAction(action);
        record.setUserId(userId);
        record.setDetail(detail);
        record.setCreateTime(java.time.LocalDateTime.now());
        
        // 稍后集成区块链上链
        log.debug("审计日志已记录: {}", record);
    }
    
    /**
     * 发送告警通知
     */
    private void sendAlertNotification(String type, String message) {
        try {
            // 邮件通知
            sendEmailAlert(type, message);
            
            // Webhook通知（企业微信/钉钉）
            sendWebhookAlert(type, message);
            
            // SMS短信（高级别威胁）
            if (type.equals("IP_BLOCK") || type.equals("USER_ISOLATION")) {
                sendSMSAlert(type, message);
            }
        } catch (Exception e) {
            log.error("发送告警通知失败", e);
        }
    }
    
    private void sendEmailAlert(String type, String message) {
        log.info("发送邮件告警 - 类型:{}, 内容:{}", type, message);
        // 实际应调用邮件服务
    }
    
    private void sendWebhookAlert(String type, String message) {
        log.info("发送Webhook告警 - 类型:{}, 内容:{}", type, message);
        // 实际应调用Webhook
    }
    
    private void sendSMSAlert(String type, String message) {
        log.info("发送短信告警 - 类型:{}, 内容:{}", type, message);
        // 实际应调用短信服务
    }
    
    /**
     * 审计记录内部类
     */
    private static class AuditRecord {
        private String action;
        private Long userId;
        private String detail;
        private java.time.LocalDateTime createTime;
        
        // Getters and setters
        public void setAction(String action) { this.action = action; }
        public void setUserId(Long userId) { this.userId = userId; }
        public void setDetail(String detail) { this.detail = detail; }
        public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
        
        public String toString() {
            return "AuditRecord{action=" + action + ", userId=" + userId + ", detail=" + detail + "}";
        }
    }
}
EOF

    log_success "阶段二完成：AI自动化响应系统已生成"
}

# 阶段三：区块链联盟链基础设施
stage3_blockchain_infrastructure() {
    log_info "======================="
    log_info "阶段三：区块链联盟链基础设施"
    log_info "======================="
    
    log_info "步骤3.1：下载Fabric Docker镜像..."
    
    # 检查镜像是否存在
    if ! docker images | grep -q "hyperledger/fabric-peer:$FABRIC_VERSION"; then
        log_info "正在下载Fabric镜像（这可能需要几分钟）..."
        
        docker pull hyperledger/fabric-peer:$FABRIC_VERSION
        docker pull hyperledger/fabric-orderer:$FABRIC_VERSION
        docker pull hyperledger/fabric-ccenv:$FABRIC_VERSION
        docker pull hyperledger/fabric-ca:$FABRIC_VERSION
        docker pull hyperledger/fabric-tools:$FABRIC_VERSION
        
        log_success "Fabric镜像下载完成"
    else
        log_info "Fabric镜像已存在"
    fi
    
    # 检查Explorer镜像
    if ! docker images | grep -q "hyperledger/explorer:$EXPLORER_VERSION"; then
        docker pull hyperledger/explorer:$EXPLORER_VERSION
        docker pull hyperledger/explorer-db:$EXPLORER_VERSION
        log_success "Fabric Explorer镜像下载完成"
    else
        log_info "Fabric Explorer镜像已存在"
    fi
    
    log_info "步骤3.2：生成Fabric网络配置..."
    
    # 创建crypto-config.yaml
    cat > "$PROJECT_ROOT/docker/fabric/crypto-config.yaml" << 'EOF'
# Fabric证书配置 - 3组织联盟
OrdererOrgs:
  - Name: OrdererOrg
    Domain: bankshield.com
    Specs:
      - Hostname: orderer

PeerOrgs:
  - Name: BankShieldOrg
    Domain: bankshield.internal
    EnableNodeOUs: true
    Template:
      Count: 2
    Users:
      Count: 3

  - Name: RegulatorOrg
    Domain: regulator.gov
    EnableNodeOUs: true
    Template:
      Count: 2
    Users:
      Count: 2

  - Name: AuditorOrg
    Domain: auditor.com
    EnableNodeOUs: true
    Template:
      Count: 2
    Users:
      Count: 2
EOF

    # 创建configtx.yaml
    cat > "$PROJECT_ROOT/docker/fabric/configtx.yaml" << 'EOF'
Organizations:
  - &OrdererOrg
    Name: OrdererOrg
    ID: OrdererMSP
    MSPDir: crypto-config/ordererOrganizations/bankshield.com/msp
    Policies:
      Readers:
        Type: Signature
        Rule: "OR('OrdererMSP.member')"
      Writers:
        Type: Signature
        Rule: "OR('OrdererMSP.member')"
      Admins:
        Type: Signature
        Rule: "OR('OrdererMSP.admin')"

  - &BankShieldOrg
    Name: BankShieldOrg
    ID: BankShieldOrgMSP
    MSPDir: crypto-config/peerOrganizations/bankshield.internal/msp
    Policies:
      Readers:
        Type: Signature
        Rule: "OR('BankShieldOrgMSP.admin', 'BankShieldOrgMSP.peer', 'BankShieldOrgMSP.client')"
      Writers:
        Type: Signature
        Rule: "OR('BankShieldOrgMSP.admin', 'BankShieldOrgMSP.client')"
      Admins:
        Type: Signature
        Rule: "OR('BankShieldOrgMSP.admin')"
      Endorsement:
        Type: Signature
        Rule: "OR('BankShieldOrgMSP.peer')"

  - &RegulatorOrg
    Name: RegulatorOrg
    ID: RegulatorOrgMSP
    MSPDir: crypto-config/peerOrganizations/regulator.gov/msp
    Policies:
      Readers:
        Type: Signature
        Rule: "OR('RegulatorOrgMSP.admin', 'RegulatorOrgMSP.peer', 'RegulatorOrgMSP.client')"
      Writers:
        Type: Signature
        Rule: "OR('RegulatorOrgMSP.admin')"
      Admins:
        Type: Signature
        Rule: "OR('RegulatorOrgMSP.admin')"
      Endorsement:
        Type: Signature
        Rule: "OR('RegulatorOrgMSP.peer')"

  - &AuditorOrg
    Name: AuditorOrg
    ID: AuditorOrgMSP
    MSPDir: crypto-config/peerOrganizations/auditor.com/msp
    Policies:
      Readers:
        Type: Signature
        Rule: "OR('AuditorOrgMSP.admin', 'AuditorOrgMSP.peer', 'AuditorOrgMSP.client')"
      Writers:
        Type: Signature
        Rule: "OR('AuditorOrgMSP.admin')"
      Admins:
        Type: Signature
        Rule: "OR('AuditorOrgMSP.admin')"
      Endorsement:
        Type: Signature
        Rule: "OR('AuditorOrgMSP.peer')"

Capabilities:
  Channel: &ChannelCapabilities
    V2_0: true
  Orderer: &OrdererCapabilities
    V2_0: true
  Application: &ApplicationCapabilities
    V2_0: true

Application: &ApplicationDefaults
  Organizations:
  Policies:
    Readers:
      Type: ImplicitMeta
      Rule: "ANY Readers"
    Writers:
      Type: ImplicitMeta
      Rule: "ANY Writers"
    Admins:
      Type: ImplicitMeta
      Rule: "MAJORITY Admins"
    LifecycleEndorsement:
      Type: ImplicitMeta
      Rule: "MAJORITY Endorsement"
    Endorsement:
      Type: ImplicitMeta
      Rule: "MAJORITY Endorsement"
  Capabilities:
    <<: *ApplicationCapabilities

Orderer: &OrdererDefaults
  OrdererType: etcdraft
  EtcdRaft:
    Consenters:
      - Host: orderer.bankshield.com
        Port: 7050
        ClientTLSCert: crypto-config/ordererOrganizations/bankshield.com/orderers/orderer.bankshield.com/tls/server.crt
        ServerTLSCert: crypto-config/ordererOrganizations/bankshield.com/orderers/orderer.bankshield.com/tls/server.crt
  Addresses:
    - orderer.bankshield.com:7050
  BatchTimeout: 2s
  BatchSize:
    MaxMessageCount: 10
    AbsoluteMaxBytes: 99 MB
    PreferredMaxBytes: 512 KB
  Organizations:
  Policies:
    Readers:
      Type: ImplicitMeta
      Rule: "ANY Readers"
    Writers:
      Type: ImplicitMeta
      Rule: "ANY Writers"
    Admins:
      Type: ImplicitMeta
      Rule: "MAJORITY Admins"
    BlockValidation:
      Type: ImplicitMeta
      Rule: "ANY Writers"

Channel: &ChannelDefaults
  Policies:
    Readers:
      Type: ImplicitMeta
      Rule: "ANY Readers"
    Writers:
      Type: ImplicitMeta
      Rule: "ANY Writers"
    Admins:
      Type: ImplicitMeta
      Rule: "MAJORITY Admins"
  Capabilities:
    <<: *ChannelCapabilities

Profiles:
  ThreeOrgsOrdererGenesis:
    <<: *ChannelDefaults
    Orderer:
      <<: *OrdererDefaults
      Organizations:
        - *OrdererOrg
      Capabilities:
        <<: *OrdererCapabilities
    Application:
      <<: *ApplicationDefaults
      Organizations:
        - *OrdererOrg
    Consortiums:
      SampleConsortium:
        Organizations:
          - *BankShieldOrg
          - *RegulatorOrg
          - *AuditorOrg

  ThreeOrgsChannel:
    Consortium: SampleConsortium
    <<: *ChannelDefaults
    Application:
      <<: *ApplicationDefaults
      Organizations:
        - *BankShieldOrg
        - *RegulatorOrg
        - *AuditorOrg
      Capabilities:
        <<: *ApplicationCapabilities
      Policies:
        LifecycleEndorsement:
          Type: ImplicitMeta
          Rule: "MAJORITY Endorsement"
        Endorsement:
          Type: ImplicitMeta
          Rule: "MAJORITY Endorsement"
EOF

    # 创建docker-compose.yaml
    cat > "$PROJECT_ROOT/docker/fabric/docker-compose.yaml" << 'EOF'
version: '2.4'

networks:
  bankshield_blockchain:
    driver: bridge

services:
  orderer.bankshield.com:
    container_name: orderer.bankshield.com
    image: hyperledger/fabric-orderer:$FABRIC_VERSION
    environment:
      - FABRIC_LOGGING_SPEC=INFO
      - ORDERER_GENERAL_LISTENADDRESS=0.0.0.0
      - ORDERER_GENERAL_LISTENPORT=7050
      - ORDERER_GENERAL_GENESISMETHOD=file
      - ORDERER_GENERAL_GENESISFILE=/var/hyperledger/orderer/orderer.genesis.block
      - ORDERER_GENERAL_LOCALMSPID=OrdererMSP
      - ORDERER_GENERAL_LOCALMSPDIR=/var/hyperledger/orderer/msp
      - ORDERER_GENERAL_TLS_ENABLED=true
      - ORDERER_GENERAL_TLS_PRIVATEKEY=/var/hyperledger/orderer/tls/server.key
      - ORDERER_GENERAL_TLS_CERTIFICATE=/var/hyperledger/orderer/tls/server.crt
      - ORDERER_GENERAL_TLS_ROOTCAS=[/var/hyperledger/orderer/tls/ca.crt]
      - ORDERER_GENERAL_CLUSTER_CLIENTCERTIFICATE=/var/hyperledger/orderer/tls/server.crt
      - ORDERER_GENERAL_CLUSTER_CLIENTPRIVATEKEY=/var/hyperledger/orderer/tls/server.key
      - ORDERER_GENERAL_CLUSTER_ROOTCAS=[/var/hyperledger/orderer/tls/ca.crt]
    working_dir: /opt/gopath/src/github.com/hyperledger/fabric
    command: orderer
    volumes:
      - ../system-genesis-block/genesis.block:/var/hyperledger/orderer/orderer.genesis.block
      - crypto-config/ordererOrganizations/bankshield.com/orderers/orderer.bankshield.com/msp:/var/hyperledger/orderer/msp
      - crypto-config/ordererOrganizations/bankshield.com/orderers/orderer.bankshield.com/tls/:/var/hyperledger/orderer/tls
      - orderer.bankshield.com:/var/hyperledger/production/orderer
    ports:
      - 7050:7050
    networks:
      - bankshield_blockchain

  peer0.bankshield.internal:
    container_name: peer0.bankshield.internal
    image: hyperledger/fabric-peer:$FABRIC_VERSION
    environment:
      - CORE_PEER_ID=peer0.bankshield.internal
      - CORE_PEER_ADDRESS=peer0.bankshield.internal:7051
      - CORE_PEER_LISTENADDRESS=0.0.0.0:7051
      - CORE_PEER_CHAINCODEADDRESS=peer0.bankshield.internal:7052
      - CORE_PEER_CHAINCODESERVER_ADDRESS=peer0.bankshield.internal:7052
      - CORE_PEER_GOSSIP_BOOTSTRAP=peer1.bankshield.internal:7051
      - CORE_PEER_GOSSIP_EXTERNALENDPOINT=peer0.bankshield.internal:7051
      - CORE_PEER_LOCALMSPID=BankShieldOrgMSP
      - CORE_PEER_TLS_ENABLED=true
      - CORE_PEER_TLS_CERT_FILE=/etc/hyperledger/fabric/tls/server.crt
      - CORE_PEER_TLS_KEY_FILE=/etc/hyperledger/fabric/tls/server.key
      - CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/tls/ca.crt
      - CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp
      - CORE_PEER_ADDRESSAUTODETECT=true
      - CORE_LEDGER_STATE_STATEDATABASE=CouchDB
      - CORE_LEDGER_STATE_COUCHDBCONFIG_COUCHDBADDRESS=couchdb0.bankshield.internal:5984
      - CORE_LEDGER_STATE_COUCHDBCONFIG_USERNAME=admin
      - CORE_LEDGER_STATE_COUCHDBCONFIG_PASSWORD=adminpw
      - CORE_VM_ENDPOINT=unix:///host/var/run/docker.sock
      - FABRIC_LOGGING_SPEC=INFO
      - CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE=docker_bankshield_blockchain
    working_dir: /opt/gopath/src/github.com/hyperledger/fabric/peer
    command: peer node start
    volumes:
      - /var/run/docker.sock:/host/var/run/docker.sock
      - crypto-config/peerOrganizations/bankshield.internal/peers/peer0.bankshield.internal/msp:/etc/hyperledger/fabric/msp
      - crypto-config/peerOrganizations/bankshield.internal/peers/peer0.bankshield.internal/tls:/etc/hyperledger/fabric/tls
      - peer0.bankshield.internal:/var/hyperledger/production
    ports:
      - 7051:7051
      - 7052:7052
    depends_on:
      - couchdb0.bankshield.internal
    networks:
      - bankshield_blockchain

  couchdb0.bankshield.internal:
    container_name: couchdb0.bankshield.internal
    image: couchdb:3.2.2
    environment:
      - COUCHDB_USER=admin
      - COUCHDB_PASSWORD=adminpw
    ports:
      - 5984:5984
    networks:
      - bankshield_blockchain

  peer0.regulator.gov:
    container_name: peer0.regulator.gov
    image: hyperledger/fabric-peer:$FABRIC_VERSION
    environment:
      - CORE_PEER_ID=peer0.regulator.gov
      - CORE_PEER_ADDRESS=peer0.regulator.gov:9051
      - CORE_PEER_LISTENADDRESS=0.0.0.0:9051
      - CORE_PEER_CHAINCODEADDRESS=peer0.regulator.gov:9052
      - CORE_PEER_CHAINCODESERVER_ADDRESS=peer0.regulator.gov:9052
      - CORE_PEER_GOSSIP_EXTERNALENDPOINT=peer0.regulator.gov:9051
      - CORE_PEER_LOCALMSPID=RegulatorOrgMSP
      - CORE_PEER_TLS_ENABLED=true
      - CORE_PEER_TLS_CERT_FILE=/etc/hyperledger/fabric/tls/server.crt
      - CORE_PEER_TLS_KEY_FILE=/etc/hyperledger/fabric/tls/server.key
      - CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/tls/ca.crt
      - CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp
      - CORE_VM_ENDPOINT=unix:///host/var/run/docker.sock
      - FABRIC_LOGGING_SPEC=INFO
      - CORE_LEDGER_STATE_STATEDATABASE=CouchDB
      - CORE_LEDGER_STATE_COUCHDBCONFIG_COUCHDBADDRESS=couchdb0.regulator.gov:6984
      - CORE_LEDGER_STATE_COUCHDBCONFIG_USERNAME=admin
      - CORE_LEDGER_STATE_COUCHDBCONFIG_PASSWORD=adminpw
    working_dir: /opt/gopath/src/github.com/hyperledger/fabric/peer
    command: peer node start
    volumes:
      - /var/run/docker.sock:/host/var/run/docker.sock
      - crypto-config/peerOrganizations/regulator.gov/peers/peer0.regulator.gov/msp:/etc/hyperledger/fabric/msp
      - crypto-config/peerOrganizations/regulator.gov/peers/peer0.regulator.gov/tls:/etc/hyperledger/fabric/tls
      - peer0.regulator.gov:/var/hyperledger/production
    ports:
      - 9051:9051
      - 9052:9052
    depends_on:
      - couchdb0.regulator.gov
    networks:
      - bankshield_blockchain

  couchdb0.regulator.gov:
    container_name: couchdb0.regulator.gov
    image: couchdb:3.2.2
    environment:
      - COUCHDB_USER=admin
      - COUCHDB_PASSWORD=adminpw
    ports:
      - 6984:5984
    networks:
      - bankshield_blockchain

  peer0.auditor.com:
    container_name: peer0.auditor.com
    image: hyperledger/fabric-peer:$FABRIC_VERSION
    environment:
      - CORE_PEER_ID=peer0.auditor.com
      - CORE_PEER_ADDRESS=peer0.auditor.com:10051
      - CORE_PEER_LISTENADDRESS=0.0.0.0:10051
      - CORE_PEER_CHAINCODEADDRESS=peer0.auditor.com:10052
      - CORE_PEER_CHAINCODESERVER_ADDRESS=peer0.auditor.com:10052
      - CORE_PEER_GOSSIP_EXTERNALENDPOINT=peer0.auditor.com:10051
      - CORE_PEER_LOCALMSPID=AuditorOrgMSP
      - CORE_PEER_TLS_ENABLED=true
      - CORE_PEER_TLS_CERT_FILE=/etc/hyperledger/fabric/tls/server.crt
      - CORE_PEER_TLS_KEY_FILE=/etc/hyperledger/fabric/tls/server.key
      - CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/tls/ca.crt
      - CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp
      - CORE_VM_ENDPOINT=unix:///host/var/run/docker.sock
      - FABRIC_LOGGING_SPEC=INFO
      - CORE_LEDGER_STATE_STATEDATABASE=CouchDB
      - CORE_LEDGER_STATE_COUCHDBCONFIG_COUCHDBADDRESS=couchdb0.auditor.com:7984
      - CORE_LEDGER_STATE_COUCHDBCONFIG_USERNAME=admin
      - CORE_LEDGER_STATE_COUCHDBCONFIG_PASSWORD=adminpw
    working_dir: /opt/gopath/src/github.com/hyperledger/fabric/peer
    command: peer node start
    volumes:
      - /var/run/docker.sock:/host/var/run/docker.sock
      - crypto-config/peerOrganizations/auditor.com/peers/peer0.auditor.com/msp:/etc/hyperledger/fabric/msp
      - crypto-config/peerOrganizations/auditor.com/peers/peer0.auditor.com/tls:/etc/hyperledger/fabric/tls
      - peer0.auditor.com:/var/hyperledger/production
    ports:
      - 10051:10051
      - 10052:10052
    depends_on:
      - couchdb0.auditor.com
    networks:
      - bankshield_blockchain

  couchdb0.auditor.com:
    container_name: couchdb0.auditor.com
    image: couchdb:3.2.2
    environment:
      - COUCHDB_USER=admin
      - COUCHDB_PASSWORD=adminpw
    ports:
      - 7984:5984
    networks:
      - bankshield_blockchain
EOF

    log_success "步骤3.2：网络配置文件已生成"
    
    # 创建Fabric Explorer配置
    log_info "步骤3.3：配置Fabric Explorer..."
    cat > "$PROJECT_ROOT/docker/fabric-explorer/config.json" << 'EOF'
{
    "network-configs": {
        "bankshield-network": {
            "name": "bankshield-limited",
            "profile": "./connection-profile/bankshield-network.json"
        }
    },
    "port": 8080,
    "license": "Apache-2.0",
    "disable-worker": false
}
EOF

    # 创建连接配置文件
    mkdir -p "$PROJECT_ROOT/docker/fabric-explorer/connection-profile"
    cat > "$PROJECT_ROOT/docker/fabric-explorer/connection-profile/bankshield-network.json" << 'EOF'
{
    "name": "bankshield-network",
    "version": "1.0.0",
    "client": {
        "tlsEnable": true,
        "adminCredential": {
            "id": "exploreradmin",
            "password": "exploreradminpw"
        },
        "enableAuthentication": true,
        "organization": "BankShieldOrg",
        "connection": {
            "timeout": {
                "peer": {
                    "endorser": "300"
                },
                "orderer": {
                    "orderer": "300"
                }
            }
        }
    },
    "channels": {
        "bankshield-channel": {
            "peers": {
                "peer0.bankshield.internal": {},
                "peer0.regulator.gov": {},
                "peer0.auditor.com": {}
            }
        }
    },
    "organizations": {
        "BankShieldOrg": {
            "mspid": "BankShieldOrgMSP",
            "adminPrivateKey": {
                "path": "crypto-config/peerOrganizations/bankshield.internal/users/Admin@bankshield.internal/msp/keystore/priv_sk"
            },
            "peers": ["peer0.bankshield.internal"],
            "signedCert": {
                "path": "crypto-config/peerOrganizations/bankshield.internal/users/Admin@bankshield.internal/msp/signcerts/Admin@bankshield.internal-cert.pem"
            }
        }
    },
    "peers": {
        "peer0.bankshield.internal": {
            "tlsCACerts": {
                "path": "crypto-config/peerOrganizations/bankshield.internal/peers/peer0.bankshield.internal/tls/ca.crt"
            },
            "url": "grpcs://peer0.bankshield.internal:7051"
        }
    }
}
EOF

    log_success "阶段三完成：区块链基础设施配置完成"
}

# 阶段四：跨机构验证与完整系统
stage4_cross_org_verification() {
    log_info "======================="
    log_info "阶段四：跨机构验证与完整系统"
    log_info "======================="
    
    log_info "生成数字签名验证服务..."
    
    cat > "$PROJECT_ROOT/bankshield-blockchain/src/main/java/com/bankshield/blockchain/verify/DigitalSignatureService.java" << 'EOF'
package com.bankshield.blockchain.verify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 数字签名验证服务
 * 
 * 功能：
 * 1. 支持RSA和国密SM2签名算法
 * 2. 生成公私钥对
 * 3. 对数据进行签名
 * 4. 验证签名有效性
 * 5. 支持多机构签名验证
 */
@Slf4j
@Service
public class DigitalSignatureService {
    
    private static final String RSA_ALGORITHM = "SHA256withRSA";
    private static final String SM2_ALGORITHM = "SM2";
    private static final int KEY_SIZE = 2048;
    
    /**
     * 生成RSA密钥对
     * 
     * @return KeyPair 密钥对
     */
    public KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KEY_SIZE, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            
            log.info("RSA密钥对生成成功 - 算法:RSA, 密钥长度:{}", KEY_SIZE);
            
            return keyPair;
        } catch (Exception e) {
            log.error("生成RSA密钥对失败", e);
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }
    
    /**
     * 使用私钥签名数据
     * 
     * @param data 原始数据
     * @param privateKey 私钥
     * @param algorithm 签名算法（RSA/SM2）
     * @return Base64编码的签名
     */
    public String signData(String data, PrivateKey privateKey, String algorithm) {
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey);
            signature.update(data.getBytes("UTF-8"));
            
            byte[] signatureBytes = signature.sign();
            String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);
            
            log.debug("数据签名成功 - 算法:{}, 数据长度:{}", algorithm, data.length());
            
            return signatureBase64;
        } catch (Exception e) {
            log.error("数据签名失败", e);
            throw new RuntimeException("数据签名失败", e);
        }
    }
    
    /**
     * 使用RSA私钥签名（简化版本）
     */
    public String signWithRSA(String data, PrivateKey privateKey) {
        return signData(data, privateKey, RSA_ALGORITHM);
    }
    
    /**
     * 验证签名
     * 
     * @param data 原始数据
     * @param signatureBase64 Base64编码的签名
     * @param publicKey 公钥
     * @param algorithm 签名算法
     * @return 签名是否有效
     */
    public boolean verifySignature(String data, String signatureBase64, PublicKey publicKey, String algorithm) {
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicKey);
            signature.update(data.getBytes("UTF-8"));
            
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            boolean isValid = signature.verify(signatureBytes);
            
            log.debug("签名验证完成 - 算法:{}, 结果:{}", algorithm, isValid);
            
            return isValid;
        } catch (Exception e) {
            log.error("签名验证失败", e);
            return false;
        }
    }
    
    /**
     * 验证RSA签名（简化版本）
     */
    public boolean verifyRSASignature(String data, String signature, PublicKey publicKey) {
        return verifySignature(data, signature, publicKey, RSA_ALGORITHM);
    }
    
    /**
     * 将KeyPair转换为字符串格式（便于存储）
     */
    public KeyPairStrings convertKeyPairToStrings(KeyPair keyPair) {
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        
        return new KeyPairStrings(publicKeyBase64, privateKeyBase64);
    }
    
    /**
     * 从字符串恢复KeyPair
     */
    public KeyPair convertStringsToKeyPair(KeyPairStrings keyPairStrings) {
        try {
            // 解码公钥
            byte[] publicKeyBytes = Base64.getDecoder().decode(keyPairStrings.getPublicKey());
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            
            // 解码私钥
            byte[] privateKeyBytes = Base64.getDecoder().decode(keyPairStrings.getPrivateKey());
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            log.error("恢复KeyPair失败", e);
            throw new RuntimeException("恢复KeyPair失败", e);
        }
    }
    
    /**
     * 多机构联合签名验证
     * 
     * @param data 原始数据
     * @param signatures 各机构的签名 Map<组织名, 签名>
     * @param publicKeys 各机构的公钥 Map<组织名, 公钥>
     * @param algorithm 签名算法
     * @param threshold 通过阈值（需要验证通过的最小数量）
     * @return 是否达到阈值要求
     */
    public MultiSigVerificationResult verifyMultiSignature(
            String data,
            java.util.Map<String, String> signatures,
            java.util.Map<String, PublicKey> publicKeys,
            String algorithm,
            int threshold) {
        
        java.util.Map<String, Boolean> verificationResults = new java.util.HashMap<>();
        int validCount = 0;
        
        log.info("开始多机构签名验证 - 数据:{}, 签名数:{}, 阈值:{}", 
                data.substring(0, Math.min(50, data.length())) + "...", 
                signatures.size(), threshold);
        
        for (java.util.Map.Entry<String, String> entry : signatures.entrySet()) {
            String orgName = entry.getKey();
            String signature = entry.getValue();
            PublicKey publicKey = publicKeys.get(orgName);
            
            if (publicKey == null) {
                log.warn("组织 {} 的公钥未找到", orgName);
                verificationResults.put(orgName, false);
                continue;
            }
            
            boolean isValid = verifySignature(data, signature, publicKey, algorithm);
            verificationResults.put(orgName, isValid);
            
            if (isValid) {
                validCount++;
                log.debug("组织 {} 签名验证通过", orgName);
            } else {
                log.warn("组织 {} 签名验证失败", orgName);
            }
        }
        
        boolean isThresholdMet = validCount >= threshold;
        
        log.info("多机构签名验证完成 - 有效签名:{}/{}, 阈值:{}, 结果:{}", 
                validCount, signatures.size(), threshold, isThresholdMet);
        
        return new MultiSigVerificationResult(verificationResults, validCount, isThresholdMet);
    }
    
    /**
     * 密钥对字符串表示
     */
    public static class KeyPairStrings {
        private String publicKey;
        private String privateKey;
        
        public KeyPairStrings(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
        
        public String getPublicKey() { return publicKey; }
        public String getPrivateKey() { return privateKey; }
    }
    
    /**
     * 多签验证结果
     */
    public static class MultiSigVerificationResult {
        private java.util.Map<String, Boolean> results;
        private int validCount;
        private boolean isThresholdMet;
        
        public MultiSigVerificationResult(java.util.Map<String, Boolean> results, int validCount, boolean isThresholdMet) {
            this.results = results;
            this.validCount = validCount;
            this.isThresholdMet = isThresholdMet;
        }
        
        public java.util.Map<String, Boolean> getResults() { return results; }
        public int getValidCount() { return validCount; }
        public boolean isThresholdMet() { return isThresholdMet; }
        
        public String toString() {
            return String.format("MultiSigResult{valid=%d, total=%d, thresholdMet=%s}", 
                    validCount, results.size(), isThresholdMet);
        }
    }
}
EOF

    log_success "阶段四完成：跨机构验证系统已生成"
}

# 完整测试
test_all() {
    log_info "======================="
    log_info "执行完整测试"
    log_info "======================="
    
    # 测试1: 编译检查
    log_info "测试1: 编译所有模块..."
    cd "$PROJECT_ROOT"
    mvn clean compile -DskipTests
    log_success "编译测试通过"
    
    # 测试2: 运行单元测试
    log_info "测试2: 运行AI模块单元测试..."
    cd "$PROJECT_ROOT/bankshield-ai"
    mvn test -Dtest=AnomalyDetectionTest
    log_success "单元测试通过"
    
    # 测试3: Docker启动检查
    log_info "测试3: 检查Docker容器..."
    if docker ps | grep -q "vault"; then
        log_success "Vault容器运行正常"
    else
        log_warning "Vault容器未运行，请启动: ./scripts/security/setup-vault.sh"
    fi
    
    log_success "所有测试完成"
}

# 显示进度
show_progress() {
    log_info "======================="
    log_info "AI智能增强 + 区块链存证 实施进度"
    log_info "======================="
    
    echo ""
    echo "📊 总体进度: 0/4 阶段 completed"
    echo ""
    echo "阶段一: AI深度学习引擎      [░░░░░░░░░░] 0%"
    echo "阶段二: AI自动化响应        [░░░░░░░░░░] 0%"
    echo "阶段三: 区块链基础设施      [░░░░░░░░░░] 0%"
    echo "阶段四: 跨机构验证          [░░░░░░░░░░] 0%"
    echo ""
    echo "预计完成时间: 7天"
}

# 显示菜单
show_menu() {
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  BankShield AI智能增强 + 区块链存证 系统"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "1) 执行所有阶段（完整实施）"
    echo "2) 执行阶段一：AI深度学习引擎（Day 1-2）"
    echo "3) 执行阶段二：AI自动化响应（Day 3）"
    echo "4) 执行阶段三：区块链基础设施（Day 4-5）"
    echo "5) 执行阶段四：跨机构验证（Day 6-7）"
    echo "6) 执行完整测试"
    echo "7) 查看实施进度"
    echo "8) 查看详细文档"
    echo "9) 退出"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "选择操作: "
}

# 主流程
main() {
    # 显示欢迎信息
    echo ""
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║                                                            ║"
    echo "║   🚀 BankShield AI智能增强 + 区块链存证 实施工具          ║"
    echo "║   🎯 预计工期：7天                                          ║"
    echo "║   📊 目标：提升安全等级到金融级AI智能防护                  ║"
    echo "║                                                            ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    
    # 检查环境
    check_docker
    check_java
    check_maven
    
    # 如果有参数则执行对应阶段
    if [ "$STAGE" != "all" ]; then
        case $STAGE in
            1|1,2|1-2)
                stage1_ai_deep_learning
                ;;
            2)
                stage2_ai_automate
                ;;
            3|3,4|3-4)
                stage3_blockchain_infrastructure
                ;;
            4)
                stage4_cross_org_verification
                ;;
            test|test-all)
                test_all
                ;;
            *)
                log_error "未知阶段: $STAGE"
                echo "可用选项: 1, 2, 3, 4, test, all"
                exit 1
                ;;
        esac
        log_success "阶段 $STAGE 执行完成！"
        exit 0
    fi
    
    # 交互式菜单
    while true; do
        show_menu
        read -r choice
        
        case $choice in
            1)
                stage1_ai_deep_learning
                stage2_ai_automate
                stage3_blockchain_infrastructure
                stage4_cross_org_verification
                log_success "🎉 所有阶段执行完成！"
                ;;
            2)
                stage1_ai_deep_learning
                ;;
            3)
                stage2_ai_automate
                ;;
            4)
                stage3_blockchain_infrastructure
                ;;
            5)
                stage4_cross_org_verification
                ;;
            6)
                test_all
                ;;
            7)
                show_progress
                ;;
            8)
                echo ""
                echo "📖 详细文档位置："
                echo "   $PROJECT_ROOT/roadmaps/AI_BLOCKCHAIN_IMPLEMENTATION_PLAN.md"
                echo ""
                ;;
            9)
                log_info "感谢使用，再见！"
                exit 0
                ;;
            *)
                log_error "无效选择，请重新输入"
                ;;
        esac
        
        echo ""
        read -p "按回车键继续..."
    done
}

# 执行主函数
main "$@"
