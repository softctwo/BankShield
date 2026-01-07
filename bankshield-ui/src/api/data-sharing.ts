import request from '@/utils/request'

// 机构管理API
export function getInstitutions(params: any) {
  return request({
    url: '/api/data-sharing/institutions',
    method: 'get',
    params
  })
}

export function getInstitutionById(id: number) {
  return request({
    url: `/api/data-sharing/institutions/${id}`,
    method: 'get'
  })
}

export function createInstitution(data: any) {
  return request({
    url: '/api/data-sharing/institutions',
    method: 'post',
    data
  })
}

export function updateInstitution(id: number, data: any) {
  return request({
    url: `/api/data-sharing/institutions/${id}`,
    method: 'put',
    data
  })
}

export function deleteInstitution(id: number) {
  return request({
    url: `/api/data-sharing/institutions/${id}`,
    method: 'delete'
  })
}

// 共享协议API
export function getAgreements(params: any) {
  return request({
    url: '/api/data-sharing/agreements',
    method: 'get',
    params
  })
}

export function getAgreementById(id: number) {
  return request({
    url: `/api/data-sharing/agreements/${id}`,
    method: 'get'
  })
}

export function createAgreement(data: any) {
  return request({
    url: '/api/data-sharing/agreements',
    method: 'post',
    data
  })
}

export function updateAgreement(id: number, data: any) {
  return request({
    url: `/api/data-sharing/agreements/${id}`,
    method: 'put',
    data
  })
}

export function deleteAgreement(id: number) {
  return request({
    url: `/api/data-sharing/agreements/${id}`,
    method: 'delete'
  })
}

export function submitAgreement(id: number) {
  return request({
    url: `/api/data-sharing/agreements/${id}/submit`,
    method: 'post'
  })
}

export function approveAgreement(id: number, approved: boolean, comment?: string) {
  return request({
    url: `/api/data-sharing/agreements/${id}/approve`,
    method: 'post',
    params: { approved, comment }
  })
}

// 数据共享请求API
export function getRequests(params: any) {
  return request({
    url: '/api/data-sharing/requests',
    method: 'get',
    params
  })
}

export function getRequestById(id: number) {
  return request({
    url: `/api/data-sharing/requests/${id}`,
    method: 'get'
  })
}

export function createRequest(data: any) {
  return request({
    url: '/api/data-sharing/requests',
    method: 'post',
    data
  })
}

export function approveRequest(id: number, approved: boolean, comment?: string) {
  return request({
    url: `/api/data-sharing/requests/${id}/approve`,
    method: 'post',
    params: { approved, comment }
  })
}

export function processRequest(id: number) {
  return request({
    url: `/api/data-sharing/requests/${id}/process`,
    method: 'post'
  })
}

export function downloadRequestData(id: number) {
  return request({
    url: `/api/data-sharing/requests/${id}/download`,
    method: 'get'
  })
}

// 统计分析API
export function getOverviewStatistics() {
  return request({
    url: '/api/data-sharing/statistics/overview',
    method: 'get'
  })
}

export function getInstitutionStatistics(institutionId: number, params: any) {
  return request({
    url: `/api/data-sharing/statistics/institution/${institutionId}`,
    method: 'get',
    params
  })
}

export function getSharingTrend(params: any) {
  return request({
    url: '/api/data-sharing/statistics/trend',
    method: 'get',
    params
  })
}
