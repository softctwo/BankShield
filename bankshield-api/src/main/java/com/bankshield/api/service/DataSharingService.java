package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.DataSharingAgreement;
import com.bankshield.api.entity.DataSharingRequest;
import com.bankshield.api.entity.Institution;

import java.util.List;
import java.util.Map;

/**
 * 跨机构数据共享服务接口
 */
public interface DataSharingService {

    // 机构管理
    IPage<Institution> pageInstitutions(Page<Institution> page, String institutionName, String institutionType, String status);
    Institution getInstitutionById(Long id);
    void createInstitution(Institution institution);
    void updateInstitution(Institution institution);
    void deleteInstitution(Long id);

    // 协议管理
    IPage<DataSharingAgreement> pageAgreements(Page<DataSharingAgreement> page, String agreementName, 
                                                Long providerInstitutionId, Long consumerInstitutionId, String status);
    DataSharingAgreement getAgreementById(Long id);
    void createAgreement(DataSharingAgreement agreement);
    void updateAgreement(DataSharingAgreement agreement);
    void deleteAgreement(Long id);
    void submitAgreementForApproval(Long id);
    void approveAgreement(Long id, Boolean approved, String comment);

    // 请求管理
    IPage<DataSharingRequest> pageRequests(Page<DataSharingRequest> page, Long agreementId, 
                                           Long requesterInstitutionId, String requestType, String status);
    DataSharingRequest getRequestById(Long id);
    void createRequest(DataSharingRequest request);
    void approveRequest(Long id, Boolean approved, String comment);
    void processRequest(Long id);
    String downloadRequestData(Long id);

    // 统计分析
    Map<String, Object> getOverviewStatistics();
    Map<String, Object> getInstitutionStatistics(Long institutionId, String startDate, String endDate);
    List<Map<String, Object>> getSharingTrend(String startDate, String endDate, String granularity);
}
