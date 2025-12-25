package com.bankshield.api.service;

import com.bankshield.api.entity.OperationAudit;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * 操作审计业务接口
 * 
 * @author BankShield
 */
public interface OperationAuditService extends IService<OperationAudit> {

    /**
     * 分页查询操作审计列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名
     * @param operationType 操作类型
     * @param operationModule 操作模块
     * @param status 操作结果状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    Result<Page<OperationAudit>> getOperationAuditPage(int page, int size, String username, 
                                                       String operationType, String operationModule, 
                                                       Integer status, LocalDateTime startTime, 
                                                       LocalDateTime endTime);

    /**
     * 根据ID获取操作审计详情
     * 
     * @param id 操作审计ID
     * @return 操作审计详情
     */
    Result<OperationAudit> getOperationAuditById(Long id);

    /**
     * 导出操作审计Excel
     * 
     * @param response HTTP响应
     * @param username 用户名
     * @param operationType 操作类型
     * @param operationModule 操作模块
     * @param status 操作结果状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    void exportOperationAudit(HttpServletResponse response, String username, String operationType,
                              String operationModule, Integer status, LocalDateTime startTime,
                              LocalDateTime endTime);

    /**
     * 保存操作审计记录
     * 
     * @param operationAudit 操作审计记录
     */
    void saveOperationAudit(OperationAudit operationAudit);
}