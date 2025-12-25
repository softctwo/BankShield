import request from '@/utils/request';
import { NotificationConfig, NotifyType } from '@/types/monitor';

/**
 * 通知配置API
 */

// 分页查询通知配置
export function getNotificationConfigPage(params: {
  page?: number;
  size?: number;
  notifyType?: NotifyType;
  enabled?: number;
}) {
  return request.get<{
    records: NotificationConfig[];
    total: number;
    size: number;
    current: number;
  }>('/api/notification/config/page', { params });
}

// 获取通知配置详情
export function getNotificationConfig(id: number) {
  return request.get<NotificationConfig>(`/api/notification/config/${id}`);
}

// 创建通知配置
export function createNotificationConfig(data: NotificationConfig) {
  return request.post<string>('/api/notification/config', data);
}

// 更新通知配置
export function updateNotificationConfig(id: number, data: NotificationConfig) {
  return request.put<string>(`/api/notification/config/${id}`, data);
}

// 删除通知配置
export function deleteNotificationConfig(id: number) {
  return request.delete<string>(`/api/notification/config/${id}`);
}

// 启用/禁用通知配置
export function toggleNotificationConfig(id: number, enabled: number) {
  return request.put<string>(`/api/notification/config/${id}/enable`, null, {
    params: { enabled }
  });
}

// 测试通知配置
export function testNotificationConfig(data: NotificationConfig) {
  return request.post<string>('/api/notification/test', data);
}

// 测试指定通知配置
export function testNotificationConfigById(id: number) {
  return request.post<string>(`/api/notification/config/${id}/test`);
}

// 获取所有启用的通知配置
export function getEnabledNotificationConfigs() {
  return request.get<NotificationConfig[]>('/api/notification/config/enabled');
}

// 获取支持的通知类型
export function getNotificationTypes() {
  return request.get<{
    code: NotifyType;
    name: string;
  }[]>('/api/notification/config/types');
}

// 辅助函数

// 获取通知类型标签
export function getNotifyTypeLabel(type: NotifyType): string {
  const labelMap = {
    [NotifyType.EMAIL]: '邮件',
    [NotifyType.SMS]: '短信',
    [NotifyType.WEBHOOK]: 'Webhook',
    [NotifyType.DINGTALK]: '钉钉',
    [NotifyType.WECHAT]: '企业微信'
  };
  return labelMap[type];
}

// 验证邮箱地址格式
export function validateEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// 验证手机号格式
export function validatePhone(phone: string): boolean {
  const phoneRegex = /^1[3-9]\d{9}$/;
  return phoneRegex.test(phone);
}

// 验证URL格式
export function validateUrl(url: string): boolean {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
}

// 解析通知配置参数
export function parseNotificationConfigParams(configParams: string): Record<string, any> {
  try {
    return JSON.parse(configParams || '{}');
  } catch {
    return {};
  }
}

// 格式化通知配置参数
export function formatNotificationConfigParams(params: Record<string, any>): string {
  try {
    return JSON.stringify(params, null, 2);
  } catch {
    return '{}';
  }
}

// 获取默认通知模板
export function getDefaultNotifyTemplate(notifyType: NotifyType): string {
  const templates = {
    [NotifyType.EMAIL]: '系统告警: {{title}}\n\n告警内容: {{content}}\n告警级别: {{level}}\n告警时间: {{time}}\n\n请及时处理此告警。',
    [NotifyType.SMS]: '系统告警: {{title}}，级别: {{level}}，时间: {{time}}',
    [NotifyType.WEBHOOK]: '{"title": "{{title}}", "content": "{{content}}", "level": "{{level}}", "time": "{{time}}"}',
    [NotifyType.DINGTALK]: '## 系统告警\n\n**告警标题:** {{title}}\n\n**告警内容:** {{content}}\n\n**告警级别:** {{level}}\n\n**告警时间:** {{time}}',
    [NotifyType.WECHAT]: '系统告警\n告警标题: {{title}}\n告警内容: {{content}}\n告警级别: {{level}}\n告警时间: {{time}}'
  };
  return templates[notifyType];
}

// 获取默认配置参数
export function getDefaultConfigParams(notifyType: NotifyType): Record<string, any> {
  const params = {
    [NotifyType.EMAIL]: {
      smtp_host: 'smtp.example.com',
      smtp_port: 465,
      smtp_username: 'alert@example.com',
      smtp_password: 'password',
      smtp_ssl: true
    },
    [NotifyType.SMS]: {
      provider: 'aliyun',
      access_key: '',
      secret_key: '',
      sign_name: 'BankShield',
      template_code: ''
    },
    [NotifyType.WEBHOOK]: {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      timeout: 30000
    },
    [NotifyType.DINGTALK]: {
      webhook_url: 'https://oapi.dingtalk.com/robot/send',
      access_token: '',
      secret: ''
    },
    [NotifyType.WECHAT]: {
      webhook_url: 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send',
      access_token: ''
    }
  };
  return params[notifyType];
}