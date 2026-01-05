import request, { type ResponseData } from '@/utils/request'
import type { MPC, PageData, PageParams } from '@/types/api'

/**
 * 多方安全计算API
 */

// 执行隐私求交
export function performPSI(data: MPC.PSIRequest): Promise<ResponseData<MPC.Job>> {
  return request({
    url: '/api/mpc/psi',
    method: 'post',
    data
  })
}

// 执行安全求和
export function secureSum(data: MPC.SecureSumRequest): Promise<ResponseData<MPC.Job>> {
  return request({
    url: '/api/mpc/secure-sum',
    method: 'post',
    data
  })
}

// 执行联合查询
export function performJointQuery(data: any) {
  return request({
    url: '/api/mpc/joint-query',
    method: 'post',
    data
  })
}

// 查询任务详情
export function getJob(jobId: number): Promise<ResponseData<MPC.Job>> {
  return request({
    url: `/api/mpc/job/${jobId}`,
    method: 'get'
  })
}

// 分页查询任务列表
export function getJobs(params: PageParams & { type?: MPC.JobType; status?: MPC.JobStatus }): Promise<ResponseData<PageData<MPC.Job>>> {
  return request({
    url: '/api/mpc/jobs',
    method: 'get',
    params
  })
}

// 取消任务
export function cancelJob(jobId: number) {
  return request({
    url: `/api/mpc/job/${jobId}/cancel`,
    method: 'post'
  })
}

// 注册参与方
export function registerParty(data: any) {
  return request({
    url: '/api/mpc/party/register',
    method: 'post',
    data
  })
}

// 查询参与方列表
export function getParties(params?: any) {
  return request({
    url: '/api/mpc/parties',
    method: 'get',
    params
  })
}

// 更新参与方状态
export function updatePartyStatus(partyId: number, status: string) {
  return request({
    url: `/api/mpc/party/${partyId}/status`,
    method: 'put',
    params: { status }
  })
}

// 获取统计信息
export function getStatistics(): Promise<ResponseData<MPC.Statistics>> {
  return request({
    url: '/api/mpc/statistics',
    method: 'get'
  })
}

// 健康检查
export function healthCheck() {
  return request({
    url: '/api/mpc/health',
    method: 'get'
  })
}

// 获取协议信息
export function getProtocols() {
  return request({
    url: '/api/mpc/protocols',
    method: 'get'
  })
}
