package com.bankshield.api.controller;

import com.bankshield.api.entity.AuditLogBlock;
import com.bankshield.api.result.Result;
import com.bankshield.api.service.BlockchainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 区块链控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/blockchain")
@Api(tags = "区块链管理")
public class BlockchainController {
    
    @Autowired
    private BlockchainService blockchainService;
    
    /**
     * 创建新区块
     */
    @PostMapping("/blocks")
    @ApiOperation("创建新区块")
    public Result<AuditLogBlock> createBlock(@RequestBody List<Long> logIds) {
        try {
            AuditLogBlock block = blockchainService.createBlock(logIds);
            return Result.success(block);
        } catch (Exception e) {
            log.error("创建区块失败", e);
            return Result.error("创建区块失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取区块列表
     */
    @GetMapping("/blocks")
    @ApiOperation("获取区块列表")
    public Result<Map<String, Object>> getBlocks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = blockchainService.getBlockList(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取区块列表失败", e);
            return Result.error("获取区块列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取最新区块
     */
    @GetMapping("/blocks/latest")
    @ApiOperation("获取最新区块")
    public Result<AuditLogBlock> getLatestBlock() {
        try {
            AuditLogBlock block = blockchainService.getLatestBlock();
            if (block == null) {
                return Result.error("区块链为空");
            }
            return Result.success(block);
        } catch (Exception e) {
            log.error("获取最新区块失败", e);
            return Result.error("获取最新区块失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取区块详情
     */
    @GetMapping("/blocks/{id}")
    @ApiOperation("获取区块详情")
    public Result<Map<String, Object>> getBlockDetail(@PathVariable Long id) {
        try {
            Map<String, Object> detail = blockchainService.getBlockDetail(id);
            if (detail.get("success") != null && !(Boolean) detail.get("success")) {
                return Result.error(detail.get("message").toString());
            }
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取区块详情失败", e);
            return Result.error("获取区块详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据区块索引获取区块
     */
    @GetMapping("/blocks/index/{blockIndex}")
    @ApiOperation("根据区块索引获取区块")
    public Result<AuditLogBlock> getBlockByIndex(@PathVariable Long blockIndex) {
        try {
            AuditLogBlock block = blockchainService.getBlockByIndex(blockIndex);
            if (block == null) {
                return Result.error("区块不存在");
            }
            return Result.success(block);
        } catch (Exception e) {
            log.error("获取区块失败", e);
            return Result.error("获取区块失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证单个区块
     */
    @PostMapping("/verify/block/{id}")
    @ApiOperation("验证单个区块")
    public Result<Map<String, Object>> verifyBlock(@PathVariable Long id) {
        try {
            boolean isValid = blockchainService.verifyBlock(id);
            Map<String, Object> result = Map.of(
                "blockId", id,
                "isValid", isValid,
                "message", isValid ? "区块验证通过" : "区块验证失败"
            );
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证区块失败", e);
            return Result.error("验证区块失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证整条区块链
     */
    @PostMapping("/verify/chain")
    @ApiOperation("验证整条区块链")
    public Result<Map<String, Object>> verifyBlockchain() {
        try {
            Map<String, Object> result = blockchainService.verifyBlockchain();
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证区块链失败", e);
            return Result.error("验证区块链失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证区块范围
     */
    @PostMapping("/verify/range")
    @ApiOperation("验证区块范围")
    public Result<Map<String, Object>> verifyBlockchainRange(
            @RequestParam Long startIndex,
            @RequestParam Long endIndex) {
        try {
            Map<String, Object> result = blockchainService.verifyBlockchainRange(startIndex, endIndex);
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证区块范围失败", e);
            return Result.error("验证区块范围失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取区块链统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation("获取区块链统计信息")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = blockchainService.getBlockchainStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算区块哈希
     */
    @PostMapping("/blocks/hash")
    @ApiOperation("计算区块哈希")
    public Result<Map<String, Object>> calculateHash(@RequestBody AuditLogBlock block) {
        try {
            String hash = blockchainService.calculateBlockHash(block);
            Map<String, Object> result = Map.of(
                "blockIndex", block.getBlockIndex(),
                "calculatedHash", hash
            );
            return Result.success(result);
        } catch (Exception e) {
            log.error("计算区块哈希失败", e);
            return Result.error("计算区块哈希失败: " + e.getMessage());
        }
    }
}
