// BankShield k6性能测试脚本
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 自定义指标
const errorRate = new Rate('errors');
const apiResponseTime = new Trend('api_response_time');

// 测试配置
export const options = {
  stages: [
    { duration: '2m', target: 100 },   // 预热阶段
    { duration: '5m', target: 500 },   // 加压阶段
    { duration: '5m', target: 1000 },  // 峰值阶段
    { duration: '2m', target: 0 },     // 恢复阶段
  ],
  
  thresholds: {
    // 全局阈值
    'http_req_duration': ['p(95)<200'],     // 95%请求响应时间小于200ms
    'http_req_failed': ['rate<0.01'],       // 错误率小于1%
    'errors': ['rate<0.01'],                 // 自定义错误率小于1%
    
    // 按业务分组设置不同阈值
    'http_req_duration{group:user}': ['avg<100', 'p(95)<150'],
    'http_req_duration{group:key}': ['avg<150', 'p(95)<250'],
    'http_req_duration{group:encrypt}': ['avg<200', 'p(95)<300'],
    'http_req_duration{group:monitor}': ['avg<80', 'p(95)<120'],
    
    // API特定阈值
    'api_response_time': ['avg<150', 'p(95)<250'],
  },
  
  // 其他配置
  noConnectionReuse: false,
  tlsAuth: null,
  tlsCipherSuites: null,
  tlsVersion: null,
  tlsSessionReuse: true,
};

// 全局变量
let baseUrl = __ENV.API_BASE_URL || 'http://localhost:8080';
let adminUsername = __ENV.TEST_USER || 'admin';
let adminPassword = __ENV.TEST_PASSWORD || '123456';
let accessToken = null;

/**
 * 初始化函数 - 获取访问令牌
 */
export function setup() {
  console.log('开始设置测试环境...');
  
  // 管理员登录获取token
  const loginResponse = http.post(`${baseUrl}/api/auth/login`, JSON.stringify({
    username: adminUsername,
    password: adminPassword
  }), {
    headers: { 'Content-Type': 'application/json' }
  });
  
  check(loginResponse, {
    '登录成功': (r) => r.status === 200,
    '获取到token': (r) => r.json().data.accessToken !== undefined
  });
  
  if (loginResponse.status === 200 && loginResponse.json().data.accessToken) {
    accessToken = loginResponse.json().data.accessToken;
    console.log('成功获取访问令牌');
    
    // 准备测试数据
    const testData = {
      users: generateTestUsers(10),
      keys: generateTestKeys(5),
      dataSamples: generateDataSamples(20)
    };
    
    return { accessToken, testData };
  } else {
    throw new Error('无法获取访问令牌，测试终止');
  }
}

/**
 * 主要测试函数
 */
export default function(data) {
  const token = data.accessToken;
  const testData = data.testData;
  
  // 设置认证头
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  };
  
  // 用户管理测试组
  group('用户管理', function() {
    // 查询用户列表
    const userListResponse = http.get(`${baseUrl}/api/user/page?page=${randomInt(1, 10)}&size=10`, { headers });
    check(userListResponse, {
      '用户列表查询成功': (r) => r.status === 200,
      '返回用户数据': (r) => r.json().data.records !== undefined
    });
    apiResponseTime.add(userListResponse.timings.duration);
    errorRate.add(userListResponse.status !== 200);
    
    sleep(randomInt(1, 3));
    
    // 创建用户
    const randomUser = testData.users[randomInt(0, testData.users.length - 1)];
    const createUserResponse = http.post(`${baseUrl}/api/user`, JSON.stringify(randomUser), { headers });
    check(createUserResponse, {
      '创建用户成功': (r) => r.status === 200,
      '返回用户ID': (r) => r.json().data !== undefined
    });
    apiResponseTime.add(createUserResponse.timings.duration);
    errorRate.add(createUserResponse.status !== 200);
    
    sleep(randomInt(1, 3));
  });
  
  // 密钥管理测试组
  group('密钥管理', function() {
    // 查询密钥列表
    const keyListResponse = http.get(`${baseUrl}/api/key/list?page=${randomInt(1, 5)}&size=10`, { headers });
    check(keyListResponse, {
      '密钥列表查询成功': (r) => r.status === 200,
      '返回密钥数据': (r) => r.json().data.list !== undefined
    });
    apiResponseTime.add(keyListResponse.timings.duration);
    errorRate.add(keyListResponse.status !== 200);
    
    sleep(randomInt(1, 3));
    
    // 生成密钥
    const randomKey = testData.keys[randomInt(0, testData.keys.length - 1)];
    const generateKeyResponse = http.post(`${baseUrl}/api/key/generate`, JSON.stringify(randomKey), { headers });
    check(generateKeyResponse, {
      '生成密钥成功': (r) => r.status === 200,
      '返回密钥ID': (r) => r.json().data !== undefined
    });
    apiResponseTime.add(generateKeyResponse.timings.duration);
    errorRate.add(generateKeyResponse.status !== 200);
    
    sleep(randomInt(1, 3));
  });
  
  // 数据加密测试组
  group('数据加密', function() {
    // 数据加密
    const randomData = testData.dataSamples[randomInt(0, testData.dataSamples.length - 1)];
    const encryptResponse = http.post(`${baseUrl}/api/encrypt/data`, JSON.stringify({
      data: randomData,
      algorithm: 'SM4',
      keyId: randomInt(1, 100)
    }), { headers });
    check(encryptResponse, {
      '数据加密成功': (r) => r.status === 200,
      '返回加密数据': (r) => r.json().data !== undefined
    });
    apiResponseTime.add(encryptResponse.timings.duration);
    errorRate.add(encryptResponse.status !== 200);
    
    sleep(randomInt(1, 3));
    
    // 数据解密
    const decryptResponse = http.post(`${baseUrl}/api/decrypt/data`, JSON.stringify({
      encryptedData: generateRandomEncryptedData(),
      algorithm: 'SM4',
      keyId: randomInt(1, 100)
    }), { headers });
    check(decryptResponse, {
      '数据解密成功': (r) => r.status === 200,
      '返回解密数据': (r) => r.json().data !== undefined
    });
    apiResponseTime.add(decryptResponse.timings.duration);
    errorRate.add(decryptResponse.status !== 200);
    
    sleep(randomInt(1, 3));
  });
  
  // 监控数据测试组
  group('监控数据', function() {
    // 获取实时监控数据
    const monitorResponse = http.get(`${baseUrl}/api/monitor/realtime`, { headers });
    check(monitorResponse, {
      '监控数据获取成功': (r) => r.status === 200,
      '返回监控数据': (r) => r.json().data !== undefined
    });
    apiResponseTime.add(monitorResponse.timings.duration);
    errorRate.add(monitorResponse.status !== 200);
    
    sleep(randomInt(1, 3));
    
    // 获取告警列表
    const alertResponse = http.get(`${baseUrl}/api/alert/list?page=1&size=20`, { headers });
    check(alertResponse, {
      '告警列表获取成功': (r) => r.status === 200,
      '返回告警数据': (r) => r.json().data !== undefined
    });
    apiResponseTime.add(alertResponse.timings.duration);
    errorRate.add(alertResponse.status !== 200);
    
    sleep(randomInt(1, 3));
  });
}

/**
 * 清理函数
 */
export function teardown(data) {
  console.log('测试完成，开始清理...');
  console.log(`总错误率: ${errorRate.value * 100}%`);
  console.log(`平均响应时间: ${apiResponseTime.avg}ms`);
}

/**
 * 辅助函数：生成测试用户数据
 */
function generateTestUsers(count) {
  const users = [];
  for (let i = 0; i < count; i++) {
    users.push({
      username: `perf_user_${i}_${Date.now()}`,
      password: 'Test@123456',
      name: `性能测试用户${i}`,
      phone: generateRandomPhone(),
      email: `perf_${i}_${Date.now()}@test.com`,
      deptId: 1,
      status: 1
    });
  }
  return users;
}

/**
 * 辅助函数：生成测试密钥数据
 */
function generateTestKeys(count) {
  const keys = [];
  for (let i = 0; i < count; i++) {
    keys.push({
      keyName: `perf_key_${i}_${Date.now()}`,
      keyType: 'SM4',
      keyUsage: 'ENCRYPT',
      keyLength: 128,
      expireDays: 365,
      rotationCycle: 90,
      description: `性能测试密钥${i}`,
      createdBy: 'perf_test'
    });
  }
  return keys;
}

/**
 * 辅助函数：生成测试数据样本
 */
function generateDataSamples(count) {
  const samples = [];
  for (let i = 0; i < count; i++) {
    samples.push(generateRandomString(randomInt(50, 200)));
  }
  return samples;
}

/**
 * 辅助函数：生成随机字符串
 */
function generateRandomString(length) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

/**
 * 辅助函数：生成随机手机号
 */
function generateRandomPhone() {
  const prefix = ['130', '131', '132', '133', '134', '135', '136', '137', '138', '139', '150', '151', '152', '153', '155', '156', '157', '158', '159', '180', '181', '182', '183', '184', '185', '186', '187', '188', '189'];
  const randomPrefix = prefix[Math.floor(Math.random() * prefix.length)];
  const randomSuffix = Math.floor(Math.random() * 100000000).toString().padStart(8, '0');
  return randomPrefix + randomSuffix;
}

/**
 * 辅助函数：生成随机加密数据
 */
function generateRandomEncryptedData() {
  return generateRandomString(randomInt(100, 300));
}

/**
 * 辅助函数：生成随机整数
 */
function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}