package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.DataSharingAgreement;
import com.bankshield.api.entity.DataSharingRequest;
import com.bankshield.api.entity.Institution;
import com.bankshield.api.service.DataSharingService;
import com.bankshield.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 跨机构数据共享控制器
 */
@Slf4j
//@RestController
@RequestMapping("/api/data-sharing")
@RequiredArgsConstructor
@Api(tags = "跨机构数据共享管理")
public class DataSharingController {

    private final DataSharingService dataSharingService;

    // ==================== 机构管理 ====================

    @GetMapping("/institutions")
    @ApiOperation("分页查询机构列表")
    @PreAuthorize("hasAuthority('data-sharing:institution:query')")
    public Result<IPage<Institution>> getInstitutions(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String institutionName,
            @RequestParam(required = false) String institutionType,
            @RequestParam(required = false) String status) {
        try {
            Page<Institution> page = new Page<>(current, size);
            IPage<Institution> result = dataSharingService.pageInstitutions(page, institutionName, institutionType, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询机构列表失败", e);
            return Result.error("查询机构列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/institutions/{id}")
    @ApiOperation("根据ID查询机构详情")
    @PreAuthorize("hasAuthority('data-sharing:institution:query')")
    public Result<Institution> getInstitutionById(@PathVariable Long id) {
        try {
            Institution institution = dataSharingService.getInstitutionById(id);
            return Result.success(institution);
        } catch (Exception e) {
            log.error("查询机构详情失败", e);
            return Result.error("查询机构详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/institutions")
    @ApiOperation("新增机构")
    @PreAuthorize("hasAuthority('data-sharing:institution:add')")
    public Result<Void> createInstitution(@Valid @RequestBody Institution institution) {
        try {
            dataSharingService.createInstitution(institution);
            return Result.success();
        } catch (Exception e) {
            log.error("新增机构失败", e);
            return Result.error("新增机构失败: " + e.getMessage());
        }
    }

    @PutMapping("/institutions/{id}")
    @ApiOperation("更新机构信息")
    @PreAuthorize("hasAuthority('data-sharing:institution:edit')")
    public Result<Void> updateInstitution(@PathVariable Long id, @Valid @RequestBody Institution institution) {
        try {
            institution.setId(id);
            dataSharingService.updateInstitution(institution);
            return Result.success();
        } catch (Exception e) {
            log.error("更新机构信息失败", e);
            return Result.error("更新机构信息失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/institutions/{id}")
    @ApiOperation("删除机构")
    @PreAuthorize("hasAuthority('data-sharing:institution:delete')")
    public Result<Void> deleteInstitution(@PathVariable Long id) {
        try {
            dataSharingService.deleteInstitution(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除机构失败", e);
            return Result.error("删除机构失败: " + e.getMessage());
        }
    }

    // ==================== 共享协议管理 ====================

    @GetMapping("/agreements")
    @ApiOperation("分页查询共享协议")
    @PreAuthorize("hasAuthority('data-sharing:agreement:query')")
    public Result<IPage<DataSharingAgreement>> getAgreements(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String agreementName,
            @RequestParam(required = false) Long providerInstitutionId,
            @RequestParam(required = false) Long consumerInstitutionId,
            @RequestParam(required = false) String status) {
        try {
            Page<DataSharingAgreement> page = new Page<>(current, size);
            IPage<DataSharingAgreement> result = dataSharingService.pageAgreements(
                    page, agreementName, providerInstitutionId, consumerInstitutionId, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询共享协议失败", e);
            return Result.error("查询共享协议失败: " + e.getMessage());
        }
    }

    @GetMapping("/agreements/{id}")
    @ApiOperation("根据ID查询协议详情")
    @PreAuthorize("hasAuthority('data-sharing:agreement:query')")
    public Result<DataSharingAgreement> getAgreementById(@PathVariable Long id) {
        try {
            DataSharingAgreement agreement = dataSharingService.getAgreementById(id);
            return Result.success(agreement);
        } catch (Exception e) {
            log.error("查询协议详情失败", e);
            return Result.error("查询协议详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/agreements")
    @ApiOperation("创建共享协议")
    @PreAuthorize("hasAuthority('data-sharing:agreement:add')")
    public Result<Void> createAgreement(@Valid @RequestBody DataSharingAgreement agreement) {
        try {
            dataSharingService.createAgreement(agreement);
            return Result.success();
        } catch (Exception e) {
            log.error("创建共享协议失败", e);
            return Result.error("创建共享协议失败: " + e.getMessage());
        }
    }

    @PutMapping("/agreements/{id}")
    @ApiOperation("更新共享协议")
    @PreAuthorize("hasAuthority('data-sharing:agreement:edit')")
    public Result<Void> updateAgreement(@PathVariable Long id, @Valid @RequestBody DataSharingAgreement agreement) {
        try {
            agreement.setId(id);
            dataSharingService.updateAgreement(agreement);
            return Result.success();
        } catch (Exception e) {
            log.error("更新共享协议失败", e);
            return Result.error("更新共享协议失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/agreements/{id}")
    @ApiOperation("删除共享协议")
    @PreAuthorize("hasAuthority('data-sharing:agreement:delete')")
    public Result<Void> deleteAgreement(@PathVariable Long id) {
        try {
            dataSharingService.deleteAgreement(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除共享协议失败", e);
            return Result.error("删除共享协议失败: " + e.getMessage());
        }
    }

    @PostMapping("/agreements/{id}/submit")
    @ApiOperation("提交协议审批")
    @PreAuthorize("hasAuthority('data-sharing:agreement:submit')")
    public Result<Void> submitAgreement(@PathVariable Long id) {
        try {
            dataSharingService.submitAgreementForApproval(id);
            return Result.success();
        } catch (Exception e) {
            log.error("提交协议审批失败", e);
            return Result.error("提交协议审批失败: " + e.getMessage());
        }
    }

    @PostMapping("/agreements/{id}/approve")
    @ApiOperation("审批协议")
    @PreAuthorize("hasAuthority('data-sharing:agreement:approve')")
    public Result<Void> approveAgreement(
            @PathVariable Long id,
            @RequestParam Boolean approved,
            @RequestParam(required = false) String comment) {
        try {
            dataSharingService.approveAgreement(id, approved, comment);
            return Result.success();
        } catch (Exception e) {
            log.error("审批协议失败", e);
            return Result.error("审批协议失败: " + e.getMessage());
        }
    }

    // ==================== 数据共享请求管理 ====================

    @GetMapping("/requests")
    @ApiOperation("分页查询共享请求")
    @PreAuthorize("hasAuthority('data-sharing:request:query')")
    public Result<IPage<DataSharingRequest>> getRequests(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long agreementId,
            @RequestParam(required = false) Long requesterInstitutionId,
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) String status) {
        try {
            Page<DataSharingRequest> page = new Page<>(current, size);
            IPage<DataSharingRequest> result = dataSharingService.pageRequests(
                    page, agreementId, requesterInstitutionId, requestType, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询共享请求失败", e);
            return Result.error("查询共享请求失败: " + e.getMessage());
        }
    }

    @GetMapping("/requests/{id}")
    @ApiOperation("根据ID查询请求详情")
    @PreAuthorize("hasAuthority('data-sharing:request:query')")
    public Result<DataSharingRequest> getRequestById(@PathVariable Long id) {
        try {
            DataSharingRequest request = dataSharingService.getRequestById(id);
            return Result.success(request);
        } catch (Exception e) {
            log.error("查询请求详情失败", e);
            return Result.error("查询请求详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/requests")
    @ApiOperation("创建数据共享请求")
    @PreAuthorize("hasAuthority('data-sharing:request:add')")
    public Result<Void> createRequest(@Valid @RequestBody DataSharingRequest request) {
        try {
            dataSharingService.createRequest(request);
            return Result.success();
        } catch (Exception e) {
            log.error("创建数据共享请求失败", e);
            return Result.error("创建数据共享请求失败: " + e.getMessage());
        }
    }

    @PostMapping("/requests/{id}/approve")
    @ApiOperation("审批共享请求")
    @PreAuthorize("hasAuthority('data-sharing:request:approve')")
    public Result<Void> approveRequest(
            @PathVariable Long id,
            @RequestParam Boolean approved,
            @RequestParam(required = false) String comment) {
        try {
            dataSharingService.approveRequest(id, approved, comment);
            return Result.success();
        } catch (Exception e) {
            log.error("审批共享请求失败", e);
            return Result.error("审批共享请求失败: " + e.getMessage());
        }
    }

    @PostMapping("/requests/{id}/process")
    @ApiOperation("处理共享请求")
    @PreAuthorize("hasAuthority('data-sharing:request:process')")
    public Result<Void> processRequest(@PathVariable Long id) {
        try {
            dataSharingService.processRequest(id);
            return Result.success();
        } catch (Exception e) {
            log.error("处理共享请求失败", e);
            return Result.error("处理共享请求失败: " + e.getMessage());
        }
    }

    @GetMapping("/requests/{id}/download")
    @ApiOperation("下载共享数据")
    @PreAuthorize("hasAuthority('data-sharing:request:download')")
    public Result<String> downloadRequestData(@PathVariable Long id) {
        try {
            String filePath = dataSharingService.downloadRequestData(id);
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("下载共享数据失败", e);
            return Result.error("下载共享数据失败: " + e.getMessage());
        }
    }

    // ==================== 统计分析 ====================

    @GetMapping("/statistics/overview")
    @ApiOperation("获取数据共享概览统计")
    @PreAuthorize("hasAuthority('data-sharing:statistics:query')")
    public Result<Map<String, Object>> getOverviewStatistics() {
        try {
            Map<String, Object> statistics = dataSharingService.getOverviewStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取概览统计失败", e);
            return Result.error("获取概览统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/institution/{institutionId}")
    @ApiOperation("获取机构数据共享统计")
    @PreAuthorize("hasAuthority('data-sharing:statistics:query')")
    public Result<Map<String, Object>> getInstitutionStatistics(
            @PathVariable Long institutionId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> statistics = dataSharingService.getInstitutionStatistics(
                    institutionId, startDate, endDate);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取机构统计失败", e);
            return Result.error("获取机构统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/trend")
    @ApiOperation("获取数据共享趋势")
    @PreAuthorize("hasAuthority('data-sharing:statistics:query')")
    public Result<List<Map<String, Object>>> getSharingTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "DAY") String granularity) {
        try {
            List<Map<String, Object>> trend = dataSharingService.getSharingTrend(
                    startDate, endDate, granularity);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取共享趋势失败", e);
            return Result.error("获取共享趋势失败: " + e.getMessage());
        }
    }
}
