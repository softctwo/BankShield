package com.bankshield.api.service;

import java.util.List;
import java.util.Map;

/**
 * 批量操作服务接口
 */
public interface BatchOperationService {

    /**
     * 批量导入数据
     *
     * @param dataType 数据类型
     * @param filePath 文件路径
     * @param options 导入选项
     * @return 导入结果
     */
    Map<String, Object> batchImport(String dataType, String filePath, Map<String, Object> options);

    /**
     * 批量导出数据
     *
     * @param dataType 数据类型
     * @param exportFormat 导出格式（EXCEL/CSV/JSON）
     * @param filters 过滤条件
     * @return 导出文件路径
     */
    String batchExport(String dataType, String exportFormat, Map<String, Object> filters);

    /**
     * 批量更新数据
     *
     * @param dataType 数据类型
     * @param updates 更新数据列表
     * @return 更新结果
     */
    Map<String, Object> batchUpdate(String dataType, List<Map<String, Object>> updates);

    /**
     * 批量删除数据
     *
     * @param dataType 数据类型
     * @param ids ID列表
     * @return 删除结果
     */
    Map<String, Object> batchDelete(String dataType, List<Long> ids);

    /**
     * 批量加密数据
     *
     * @param dataType 数据类型
     * @param fieldNames 字段名列表
     * @param algorithm 加密算法
     * @return 加密结果
     */
    Map<String, Object> batchEncrypt(String dataType, List<String> fieldNames, String algorithm);

    /**
     * 批量脱敏数据
     *
     * @param dataType 数据类型
     * @param ruleId 脱敏规则ID
     * @return 脱敏结果
     */
    Map<String, Object> batchMask(String dataType, Long ruleId);

    /**
     * 批量合规检查
     *
     * @param ruleIds 规则ID列表
     * @param targetIds 目标ID列表
     * @return 检查结果
     */
    Map<String, Object> batchComplianceCheck(List<Long> ruleIds, List<Long> targetIds);

    /**
     * 获取批量操作任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    Map<String, Object> getBatchTaskStatus(String taskId);

    /**
     * 取消批量操作任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean cancelBatchTask(String taskId);

    /**
     * 获取批量操作历史
     *
     * @param page 页码
     * @param size 每页大小
     * @return 操作历史
     */
    Map<String, Object> getBatchOperationHistory(int page, int size);
}
