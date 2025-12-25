import request from '@/utils/request'

// 角色互斥管理相关API

/**
 * 分配角色（带互斥检查）
 */
export function assignRole(data) {
  return request({
    url: '/api/role/assign',
    method: 'post',
    params: data
  })
}

/**
 * 检查用户角色合规性
 */
export function checkUserRoleCompliance(userId) {
  return request({
    url: `/api/role/check/${userId}`,
    method: 'get'
  })
}

/**
 * 查询违规记录
 */
export function getRoleViolations(params) {
  return request({
    url: '/api/role/violations',
    method: 'get',
    params
  })
}

/**
 * 处理违规记录
 */
export function handleRoleViolation(data) {
  return request({
    url: `/api/role/violation/handle/${data.id}`,
    method: 'put',
    params: data
  })
}

/**
 * 获取所有角色互斥规则
 */
export function getMutexRules() {
  return request({
    url: '/api/role/mutex-rules',
    method: 'get'
  })
}

/**
 * 获取指定角色的互斥角色
 */
export function getMutexRoles(roleCode) {
  return request({
    url: `/api/role/mutex-roles/${roleCode}`,
    method: 'get'
  })
}

/**
 * 获取三权分立状态
 */
export function getSeparationStatus() {
  return request({
    url: '/api/role/separation-status',
    method: 'get'
  })
}

/**
 * 手动触发角色检查
 */
export function triggerRoleCheck() {
  return request({
    url: '/api/role/check/trigger',
    method: 'post'
  })
}