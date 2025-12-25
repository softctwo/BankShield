package com.bankshield.api.service;

import com.bankshield.api.entity.RoleMutex;
import com.bankshield.api.entity.RoleViolation;

import java.util.List;

/**
 * 角色互斥检查服务接口
 * 提供三权分立机制的核心业务逻辑
 * 
 * @author BankShield
 * @version 1.0.0
 */
public interface RoleCheckService {

    /**
     * 检查角色分配是否违反互斥规则
     * 
     * @param userId 用户ID
     * @param roleCode 待分配的角色编码
     * @return 检查结果（true：无冲突，false：有冲突）
     */
    boolean checkRoleAssignment(Long userId, String roleCode);

    /**
     * 检查用户当前角色是否存在互斥
     * 
     * @param userId 用户ID
     * @return 互斥规则列表（空列表表示无冲突）
     */
    List<RoleMutex> checkUserRoleConflicts(Long userId);

    /**
     * 检查两个角色是否互斥
     * 
     * @param roleCode1 角色编码1
     * @param roleCode2 角色编码2
     * @return 是否互斥
     */
    boolean isRoleMutex(String roleCode1, String roleCode2);

    /**
     * 记录角色违规
     * 
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @param mutexRoleCode 互斥角色编码
     * @param violationType 违规类型
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void recordRoleViolation(Long userId, String roleCode, String mutexRoleCode, 
                           Integer violationType, Long operatorId, String operatorName);

    /**
     * 获取所有角色互斥规则
     * 
     * @return 互斥规则列表
     */
    List<RoleMutex> getAllRoleMutexRules();

    /**
     * 获取指定角色的互斥角色
     * 
     * @param roleCode 角色编码
     * @return 互斥角色编码列表
     */
    List<String> getMutexRoles(String roleCode);

    /**
     * 处理角色违规记录
     * 
     * @param violationId 违规记录ID
     * @param status 处理状态
     * @param handleRemark 处理备注
     * @param handlerName 处理人姓名
     */
    void handleRoleViolation(Long violationId, Integer status, String handleRemark, String handlerName);

    /**
     * 获取未处理的违规记录
     * 
     * @return 违规记录列表
     */
    List<RoleViolation> getUnhandledViolations();

    /**
     * 发送违规告警通知
     * 
     * @param violation 违规记录
     */
    void sendViolationAlert(RoleViolation violation);

    /**
     * 执行角色互斥检查任务
     * 扫描所有用户角色，检查是否存在违规
     */
    void executeRoleCheckJob();

    /**
     * 异步执行角色互斥检查任务
     * 使用线程池管理，防止OOM
     */
    void executeRoleCheckJobAsync();

    /**
     * 获取系统三权分立状态
     * 
     * @return 状态信息
     */
    String getSeparationOfPowersStatus();
}