package com.bankshield.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 密钥使用统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyUsageStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 密钥ID
     */
    private Long keyId;

    /**
     * 密钥名称
     */
    private String keyName;

    /**
     * 总使用次数
     */
    private Long totalUsageCount;

    /**
     * 加密操作次数
     */
    private Long encryptCount;

    /**
     * 解密操作次数
     */
    private Long decryptCount;

    /**
     * 签名操作次数
     */
    private Long signCount;

    /**
     * 验签操作次数
     */
    private Long verifyCount;

    /**
     * 操作类型统计
     */
    private List<OperationStat> operationStats;

    /**
     * 每日使用统计
     */
    private List<DailyUsageStat> dailyUsageStats;

    /**
     * 热门操作者
     */
    private List<OperatorStat> topOperators;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationStat implements Serializable {
        private String operationType;
        private Long count;
        private Long totalSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyUsageStat implements Serializable {
        private String date;
        private Long count;
        private Long totalSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatorStat implements Serializable {
        private String operator;
        private Long count;
    }
}
