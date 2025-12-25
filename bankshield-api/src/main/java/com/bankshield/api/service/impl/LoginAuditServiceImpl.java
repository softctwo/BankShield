package com.bankshield.api.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.bankshield.api.entity.LoginAudit;
import com.bankshield.api.mapper.LoginAuditMapper;
import com.bankshield.api.service.LoginAuditService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 登录审计业务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class LoginAuditServiceImpl extends ServiceImpl<LoginAuditMapper, LoginAudit> 
        implements LoginAuditService {

    @Autowired
    private LoginAuditMapper loginAuditMapper;

    @Override
    public Result<Page<LoginAudit>> getLoginAuditPage(int page, int size, String username, 
                                                     Integer status, LocalDateTime startTime, 
                                                     LocalDateTime endTime) {
        try {
            Page<LoginAudit> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<LoginAudit> wrapper = new LambdaQueryWrapper<>();

            // 添加查询条件
            if (username != null && !username.trim().isEmpty()) {
                wrapper.like(LoginAudit::getUsername, username);
            }
            if (status != null) {
                wrapper.eq(LoginAudit::getStatus, status);
            }
            if (startTime != null) {
                wrapper.ge(LoginAudit::getLoginTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(LoginAudit::getLoginTime, endTime);
            }

            // 按登录时间降序排列
            wrapper.orderByDesc(LoginAudit::getLoginTime);

            Page<LoginAudit> resultPage = loginAuditMapper.selectPage(pageParam, wrapper);
            return Result.success(resultPage);
        } catch (Exception e) {
            log.error("查询登录审计列表失败", e);
            return Result.error("查询登录审计列表失败");
        }
    }

    @Override
    public Result<LoginAudit> getLoginAuditById(Long id) {
        try {
            LoginAudit loginAudit = loginAuditMapper.selectById(id);
            if (loginAudit == null) {
                return Result.error("登录审计记录不存在");
            }
            return Result.success(loginAudit);
        } catch (Exception e) {
            log.error("查询登录审计详情失败，ID: " + id, e);
            return Result.error("查询登录审计详情失败");
        }
    }

    @Override
    public void exportLoginAudit(HttpServletResponse response, String username, Integer status,
                                LocalDateTime startTime, LocalDateTime endTime) {
        try {
            LambdaQueryWrapper<LoginAudit> wrapper = new LambdaQueryWrapper<>();

            // 添加查询条件
            if (username != null && !username.trim().isEmpty()) {
                wrapper.like(LoginAudit::getUsername, username);
            }
            if (status != null) {
                wrapper.eq(LoginAudit::getStatus, status);
            }
            if (startTime != null) {
                wrapper.ge(LoginAudit::getLoginTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(LoginAudit::getLoginTime, endTime);
            }

            // 按登录时间降序排列
            wrapper.orderByDesc(LoginAudit::getLoginTime);

            List<LoginAudit> list = loginAuditMapper.selectList(wrapper);

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("登录审计记录", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 导出Excel
            EasyExcel.write(response.getOutputStream(), LoginAudit.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("登录审计记录")
                    .doWrite(list);

            log.info("导出登录审计Excel成功，共{}条记录", list.size());
        } catch (IOException e) {
            log.error("导出登录审计Excel失败", e);
            throw new RuntimeException("导出登录审计Excel失败");
        }
    }

    @Async
    @Override
    public void saveLoginAudit(LoginAudit loginAudit) {
        try {
            loginAuditMapper.insert(loginAudit);
            log.debug("异步保存登录审计记录成功");
        } catch (Exception e) {
            log.error("异步保存登录审计记录失败", e);
        }
    }

    @Async
    @Override
    public void recordLoginSuccess(Long userId, String username, String loginIp, String loginLocation,
                                  String browser, String os) {
        try {
            LoginAudit loginAudit = LoginAudit.builder()
                    .userId(userId)
                    .username(username)
                    .loginIp(loginIp)
                    .loginLocation(loginLocation)
                    .browser(browser)
                    .os(os)
                    .status(1)
                    .loginTime(LocalDateTime.now())
                    .build();
            
            loginAuditMapper.insert(loginAudit);
            log.info("记录登录成功审计：用户{}，IP：{}", username, loginIp);
        } catch (Exception e) {
            log.error("记录登录成功审计失败", e);
        }
    }

    @Async
    @Override
    public void recordLoginFailure(String username, String loginIp, String loginLocation,
                                  String browser, String os, String failureReason) {
        try {
            LoginAudit loginAudit = LoginAudit.builder()
                    .username(username)
                    .loginIp(loginIp)
                    .loginLocation(loginLocation)
                    .browser(browser)
                    .os(os)
                    .status(0)
                    .failureReason(failureReason)
                    .loginTime(LocalDateTime.now())
                    .build();
            
            loginAuditMapper.insert(loginAudit);
            log.info("记录登录失败审计：用户{}，IP：{}，原因：{}", username, loginIp, failureReason);
        } catch (Exception e) {
            log.error("记录登录失败审计失败", e);
        }
    }

    @Async
    @Override
    public void recordLogout(Long userId, Long sessionDuration) {
        try {
            // 查找该用户最新的登录记录
            LambdaQueryWrapper<LoginAudit> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LoginAudit::getUserId, userId)
                   .eq(LoginAudit::getStatus, 1)
                   .isNull(LoginAudit::getLogoutTime)
                   .orderByDesc(LoginAudit::getLoginTime)
                   .last("LIMIT 1");
            
            LoginAudit latestLogin = loginAuditMapper.selectOne(wrapper);
            if (latestLogin != null) {
                latestLogin.setLogoutTime(LocalDateTime.now());
                latestLogin.setSessionDuration(sessionDuration);
                loginAuditMapper.updateById(latestLogin);
                log.info("记录用户退出审计：用户ID{}，会话时长{}秒", userId, sessionDuration);
            }
        } catch (Exception e) {
            log.error("记录用户退出审计失败", e);
        }
    }
}