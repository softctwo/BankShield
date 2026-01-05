package com.bankshield.api.service;

import java.util.List;
import java.util.Map;

/**
 * 数据备份和恢复服务接口
 */
public interface BackupService {

    /**
     * 创建全量备份
     *
     * @param backupName 备份名称
     * @param description 备份描述
     * @return 备份ID
     */
    Long createFullBackup(String backupName, String description);

    /**
     * 创建增量备份
     *
     * @param backupName 备份名称
     * @param baseBackupId 基础备份ID
     * @return 备份ID
     */
    Long createIncrementalBackup(String backupName, Long baseBackupId);

    /**
     * 恢复备份
     *
     * @param backupId 备份ID
     * @param targetTime 目标时间点（可选）
     * @return 是否成功
     */
    boolean restoreBackup(Long backupId, String targetTime);

    /**
     * 获取备份列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 备份列表
     */
    Map<String, Object> getBackupList(int page, int size);

    /**
     * 删除备份
     *
     * @param backupId 备份ID
     * @return 是否成功
     */
    boolean deleteBackup(Long backupId);

    /**
     * 验证备份完整性
     *
     * @param backupId 备份ID
     * @return 验证结果
     */
    Map<String, Object> verifyBackup(Long backupId);

    /**
     * 导出备份到外部存储
     *
     * @param backupId 备份ID
     * @param exportPath 导出路径
     * @return 是否成功
     */
    boolean exportBackup(Long backupId, String exportPath);

    /**
     * 从外部存储导入备份
     *
     * @param importPath 导入路径
     * @return 备份ID
     */
    Long importBackup(String importPath);

    /**
     * 获取备份统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getBackupStatistics();

    /**
     * 设置自动备份计划
     *
     * @param schedule 备份计划（cron表达式）
     * @param backupType 备份类型（FULL/INCREMENTAL）
     * @param retentionDays 保留天数
     * @return 是否成功
     */
    boolean scheduleAutoBackup(String schedule, String backupType, int retentionDays);
}
