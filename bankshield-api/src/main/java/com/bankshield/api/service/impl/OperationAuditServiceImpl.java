package com.bankshield.api.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.bankshield.api.entity.OperationAudit;
import com.bankshield.api.mapper.OperationAuditMapper;
import com.bankshield.api.service.OperationAuditService;
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
import java.util.List;

/**
 * 操作审计业务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class OperationAuditServiceImpl extends ServiceImpl<OperationAuditMapper, OperationAudit> 
        implements OperationAuditService {

    @Autowired
    private OperationAuditMapper operationAuditMapper;

    @Override
    public Result<Page<OperationAudit>> getOperationAuditPage(int page, int size, String username, 
                                                             String operationType, String operationModule, 
                                                             Integer status, LocalDateTime startTime, 
                                                             LocalDateTime endTime) {
        try {
            Page<OperationAudit> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<OperationAudit> wrapper = new LambdaQueryWrapper<>();

            // 添加查询条件
            if (username != null && !username.trim().isEmpty()) {
                wrapper.like(OperationAudit::getUsername, username);
            }
            if (operationType != null && !operationType.trim().isEmpty()) {
                wrapper.eq(OperationAudit::getOperationType, operationType);
            }
            if (operationModule != null && !operationModule.trim().isEmpty()) {
                wrapper.eq(OperationAudit::getOperationModule, operationModule);
            }
            if (status != null) {
                wrapper.eq(OperationAudit::getStatus, status);
            }
            if (startTime != null) {
                wrapper.ge(OperationAudit::getCreateTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(OperationAudit::getCreateTime, endTime);
            }

            // 按创建时间降序排列
            wrapper.orderByDesc(OperationAudit::getCreateTime);

            Page<OperationAudit> resultPage = operationAuditMapper.selectPage(pageParam, wrapper);
            return Result.OK(resultPage);
        } catch (Exception e) {
            log.error("查询操作审计列表失败", e);
            return Result.error("查询操作审计列表失败");
        }
    }

    @Override
    public Result<OperationAudit> getOperationAuditById(Long id) {
        try {
            OperationAudit operationAudit = operationAuditMapper.selectById(id);
            if (operationAudit == null) {
                return Result.error("操作审计记录不存在");
            }
            return Result.OK(operationAudit);
        } catch (Exception e) {
            log.error("查询操作审计详情失败，ID: " + id, e);
            return Result.error("查询操作审计详情失败");
        }
    }

    @Override
    public void exportOperationAudit(HttpServletResponse response, String username, String operationType,
                                    String operationModule, Integer status, LocalDateTime startTime,
                                    LocalDateTime endTime) {
        try {
            LambdaQueryWrapper<OperationAudit> wrapper = new LambdaQueryWrapper<>();

            // 添加查询条件
            if (username != null && !username.trim().isEmpty()) {
                wrapper.like(OperationAudit::getUsername, username);
            }
            if (operationType != null && !operationType.trim().isEmpty()) {
                wrapper.eq(OperationAudit::getOperationType, operationType);
            }
            if (operationModule != null && !operationModule.trim().isEmpty()) {
                wrapper.eq(OperationAudit::getOperationModule, operationModule);
            }
            if (status != null) {
                wrapper.eq(OperationAudit::getStatus, status);
            }
            if (startTime != null) {
                wrapper.ge(OperationAudit::getCreateTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(OperationAudit::getCreateTime, endTime);
            }

            // 按创建时间降序排列
            wrapper.orderByDesc(OperationAudit::getCreateTime);

            List<OperationAudit> list = operationAuditMapper.selectList(wrapper);

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("操作审计记录", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 导出Excel
            EasyExcel.write(response.getOutputStream(), OperationAudit.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("操作审计记录")
                    .doWrite(list);

            log.info("导出操作审计Excel成功，共{}条记录", list.size());
        } catch (IOException e) {
            log.error("导出操作审计Excel失败", e);
            throw new RuntimeException("导出操作审计Excel失败");
        }
    }

    @Async
    @Override
    public void saveOperationAudit(OperationAudit operationAudit) {
        try {
            operationAuditMapper.insert(operationAudit);
            log.debug("异步保存操作审计记录成功");
        } catch (Exception e) {
            log.error("异步保存操作审计记录失败", e);
        }
    }
}