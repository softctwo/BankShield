import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// 自定义指标
const httpReqFailed = new Rate('http_req_failed');
const httpReqDuration = new Trend('http_req_duration');
const apiErrors = new Counter('api_errors');

// 测试配置
export const options = {
  stages: [
    { duration: '2m', target: 100 }, // 逐渐增加到100用户
    { duration: '5m', target: 100 }, // 保持100用户5分钟
    { duration: '2m', target: 200 }, // 逐渐增加到200用户
    { duration: '5m', target: 200 }, // 保持200用户5分钟
    { duration: '2m', target: 0 },   // 逐渐降载
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95%请求响应时间小于2秒
    http_req_failed: ['rate<0.1'],     // 错误率小于10%
    'api_errors': ['count<10'],         // API错误少于10个
  },
};

// 测试数据
const BASE_URL = __ENV.API_URL || 'http://localhost:8080/api';
const TEST_USER = {
  username: 'testuser',
  password: 'testpass123',
  email: 'test@bankshield.com'
};

// 测试场景
export default function () {
  // 1. 健康检查
  let healthResponse = http.get(`${BASE_URL}/health`);
  check(healthResponse, {
    'health check status is 200': (r) => r.status === 200,
    'health check response time < 500ms': (r) => r.timings.duration < 500,
  });

  // 2. 用户认证测试
  let loginResponse = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
    username: TEST_USER.username,
    password: TEST_USER.password
  }), {
    headers: { 'Content-Type': 'application/json' }
  });

  check(loginResponse, {
    'login status is 200': (r) => r.status === 200,
    'login response has token': (r) => JSON.parse(r.body).token !== undefined,
  });

  if (loginResponse.status !== 200) {
    apiErrors.add(1);
    httpReqFailed.add(1);
  }

  let authToken = null;
  if (loginResponse.status === 200) {
    authToken = JSON.parse(loginResponse.body).token;
  }

  // 3. 数据分类测试
  let classificationPayload = {
    data: "sensitive financial information",
    type: "PII"
  };

  let classificationResponse = http.post(`${BASE_URL}/classification/classify`, JSON.stringify(classificationPayload), {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`
    }
  });

  check(classificationResponse, {
    'classification status is 200': (r) => r.status === 200,
    'classification response has result': (r) => JSON.parse(r.body).result !== undefined,
  });

  if (classificationResponse.status !== 200) {
    apiErrors.add(1);
    httpReqFailed.add(1);
  }

  // 4. 加密测试
  let encryptionPayload = {
    data: "confidential data",
    algorithm: "AES-256"
  };

  let encryptionResponse = http.post(`${BASE_URL}/encryption/encrypt`, JSON.stringify(encryptionPayload), {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`
    }
  });

  check(encryptionResponse, {
    'encryption status is 200': (r) => r.status === 200,
    'encryption response has encrypted data': (r) => JSON.parse(r.body).encryptedData !== undefined,
  });

  if (encryptionResponse.status !== 200) {
    apiErrors.add(1);
    httpReqFailed.add(1);
  }

  // 5. 监控指标测试
  let metricsResponse = http.get(`${BASE_URL}/metrics`, {
    headers: {
      'Authorization': `Bearer ${authToken}`
    }
  });

  check(metricsResponse, {
    'metrics status is 200': (r) => r.status === 200,
    'metrics response contains prometheus data': (r) => r.body.includes('# HELP'),
  });

  // 记录自定义指标
  httpReqDuration.add(healthResponse.timings.duration);
  httpReqFailed.add(healthResponse.status !== 200 ? 1 : 0);

  // 随机等待，模拟真实用户行为
  sleep(Math.random() * 2 + 1);
}

// 设置测试
export function setup() {
  console.log('Starting performance test...');
  console.log(`Target URL: ${BASE_URL}`);
  console.log(`Test users: ${__VU}`);
  
  // 可以在这里进行测试数据准备
  return {
    baseUrl: BASE_URL,
    testUser: TEST_USER
  };
}

// 清理测试
export function teardown(data) {
  console.log('Performance test completed');
  console.log(`Test data: ${JSON.stringify(data)}`);
}