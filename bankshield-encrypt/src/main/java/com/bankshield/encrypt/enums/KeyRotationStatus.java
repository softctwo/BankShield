package com.bankshield.encrypt.enums;

import lombok.Getter;

/**
 * 密钥轮换状态枚举
 * 实现平滑密钥轮换的四个阶段状态管理
 */
@Getter
public enum KeyRotationStatus {
    /**
     * 旧密钥：活跃状态，主要加密密钥
     */
    ACTIVE("ACTIVE", "活跃状态，主要加密密钥"),
    
    /**
     * 新密钥：已生成，处于预热期
     * 新密钥已生成但暂不用于加密，等待系统验证和准备
     */
    WARMING_UP("WARMING_UP", "已生成，处于预热期"),
    
    /**
     * 新密钥：已启用，双密钥并行期
     * 新旧密钥同时用于加密，确保业务连续性
     */
    DUAL_ACTIVE("DUAL_ACTIVE", "双密钥并行期"),
    
    /**
     * 旧密钥：已禁用，仅用于解密旧数据
     * 旧密钥不再用于新数据加密，仅用于解密历史数据
     */
    DECRYPT_ONLY("DECRYPT_ONLY", "仅用于解密旧数据"),
    
    /**
     * 密钥：已过期
     * 密钥已完全下线，不再使用
     */
    EXPIRED("EXPIRED", "已过期");
    
    private final String code;
    private final String description;
    
    KeyRotationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据编码获取枚举值
     */
    public static KeyRotationStatus fromCode(String code) {
        for (KeyRotationStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid key rotation status code: " + code);
    }
    
    /**
     * 检查状态是否允许加密操作
     */
    public boolean canEncrypt() {
        return this == ACTIVE || this == DUAL_ACTIVE || this == WARMING_UP;
    }
    
    /**
     * 检查状态是否允许解密操作
     */
    public boolean canDecrypt() {
        return this == ACTIVE || this == DUAL_ACTIVE || this == DECRYPT_ONLY;
    }
}