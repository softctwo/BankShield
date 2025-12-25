package com.bankshield.api.service;

import com.bankshield.api.entity.LoginAudit;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * 登录审计业务接口
 * 
 * @author BankShield
 */
public interface LoginAuditService extends IService<LoginAudit> {

    /**
     * 分页查询登录审计列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名
     * @param status 登录结果状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    Result<Page<LoginAudit>> getLoginAuditPage(int page, int size, String username, 
                                               Integer status, LocalDateTime startTime, 
                                               LocalDateTime endTime);

    /**
     * 根据ID获取登录审计详情
     * 
     * @param id 登录审计ID
     * @return 登录审计详情
     */
    Result<LoginAudit> getLoginAuditById(Long id);

    /**
     * 导出登录审计Excel
     * 
     * @param response HTTP响应
     * @param username 用户名
     * @param status 登录结果状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    void exportLoginAudit(HttpServletResponse response, String username, Integer status,
                          LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 保存登录审计记录
     * 
     * @param loginAudit 登录审计记录
     */
    void saveLoginAudit(LoginAudit loginAudit);

    /**
     * 记录登录成功
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param loginIp 登录IP
     * @param loginLocation 登录地点
     * @param browser 浏览器
     * @param os 操作系统
     */
    void recordLoginSuccess(Long userId, String username, String loginIp, String loginLocation,
                           String browser, String os);

    /**
     * 记录登录失败
     * 
     * @param username 用户名
     * @param loginIp 登录IP
     * @param loginLocation 登录地点
     * @param browser 浏览器
     * @param os 操作系统
     * @param failureReason 失败原因
     */
    void recordLoginFailure(String username, String loginIp, String loginLocation,
                           String browser, String os, String failureReason);

    /**
     * 记录用户退出
     * 
     * @param userId 用户ID
     * @param sessionDuration 会话时长(秒)
     */
    void recordLogout(Long userId, Long sessionDuration);
}