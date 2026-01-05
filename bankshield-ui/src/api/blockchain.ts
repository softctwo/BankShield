import request, { type ResponseData } from '@/utils/request'
import type { Blockchain, PageData, PageParams } from '@/types/api'

/**
 * 区块链存证API
 */

// 批量存证
export function batchAnchor(data: Blockchain.BatchAnchorRequest): Promise<ResponseData<Blockchain.AnchorRecord[]>> {
  return request({
    url: '/api/blockchain/anchor/batch',
    method: 'post',
    data
  })
}

// 异步存证
export function anchorAsync(data: any): Promise<ResponseData<{ jobId: string }>> {
  return request({
    url: '/api/blockchain/anchor/async',
    method: 'post',
    data
  })
}

// 存证合规检查结果
export function anchorComplianceCheck(data: any) {
  return request({
    url: '/api/blockchain/anchor/compliance',
    method: 'post',
    data
  })
}

// 验证审计日志完整性
export function verifyAuditLogIntegrity(auditId: number): Promise<ResponseData<boolean>> {
  return request({
    url: `/api/blockchain/verify/audit/${auditId}`,
    method: 'get'
  })
}

// 验证密钥事件完整性
export function verifyKeyEventIntegrity(keyId: number) {
  return request({
    url: `/api/blockchain/verify/key/${keyId}`,
    method: 'get'
  })
}

// 验证合规证书完整性
export function verifyComplianceCertificate(certificateId: number) {
  return request({
    url: `/api/blockchain/verify/certificate/${certificateId}`,
    method: 'get'
  })
}

// 查询存证记录
export function getAnchorRecord(id: number): Promise<ResponseData<Blockchain.AnchorRecord>> {
  return request({
    url: `/api/blockchain/record/${id}`,
    method: 'get'
  })
}

// 根据记录ID查询存证记录
export function getAnchorRecordByRecordId(recordId: string) {
  return request({
    url: `/api/blockchain/record/by-record-id/${recordId}`,
    method: 'get'
  })
}

// 根据交易哈希查询存证记录
export function getAnchorRecordByTxHash(txHash: string) {
  return request({
    url: `/api/blockchain/record/by-tx-hash/${txHash}`,
    method: 'get'
  })
}

// 查询存证记录列表
export function getAnchorRecords(params?: PageParams & { type?: string; status?: string }): Promise<ResponseData<PageData<Blockchain.AnchorRecord>>> {
  return request({
    url: '/api/blockchain/records',
    method: 'get',
    params
  })
}

// 获取区块链网络状态
export function getNetworkStatus(): Promise<ResponseData<Blockchain.NetworkStatus>> {
  return request({
    url: '/api/blockchain/network/status',
    method: 'get'
  })
}

// 获取交易详情
export function getTransaction(txHash: string) {
  return request({
    url: `/api/blockchain/transaction/${txHash}`,
    method: 'get'
  })
}

// 生成存证证书
export function generateCertificate(recordId: string) {
  return request({
    url: `/api/blockchain/certificate/generate/${recordId}`,
    method: 'get'
  })
}

// 验证审计区块完整性
export function verifyAuditBlock(blockId: string) {
  return request({
    url: `/api/blockchain/verify/block/${blockId}`,
    method: 'get'
  })
}

// 批量验证多个区块
export function verifyMultipleBlocks(blockIds: string[]) {
  return request({
    url: '/api/blockchain/verify/blocks',
    method: 'post',
    data: blockIds
  })
}

// 查询审计追踪历史
export function queryAuditTrail(userId: string, startTime: number, endTime: number) {
  return request({
    url: `/api/blockchain/audit-trail/${userId}`,
    method: 'get',
    params: { startTime, endTime }
  })
}

// 生成监管报告
export function generateRegulatoryReport(regulator: string, startTime: number, endTime: number) {
  return request({
    url: '/api/blockchain/regulatory-report',
    method: 'get',
    params: { regulator, startTime, endTime }
  })
}

// 合规性检查
export function checkCompliance(standard: string) {
  return request({
    url: '/api/blockchain/compliance/check',
    method: 'get',
    params: { standard }
  })
}

// 获取统计信息
export function getStatistics(): Promise<ResponseData<Blockchain.Statistics>> {
  return request({
    url: '/api/blockchain/statistics',
    method: 'get'
  })
}

// 健康检查
export function healthCheck() {
  return request({
    url: '/api/blockchain/health',
    method: 'get'
  })
}
