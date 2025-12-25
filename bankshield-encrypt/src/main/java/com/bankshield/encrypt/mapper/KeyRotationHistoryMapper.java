package com.bankshield.encrypt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.encrypt.entity.KeyRotationHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 密钥轮换历史Mapper
 */
@Mapper
public interface KeyRotationHistoryMapper extends BaseMapper<KeyRotationHistory> {
    
    /**
     * 分页查询轮换历史
     */
    IPage<KeyRotationHistory> selectRotationHistoryPage(Page<KeyRotationHistory> page,
                                                       @Param("oldKeyId") Long oldKeyId,
                                                       @Param("newKeyId") Long newKeyId,
                                                       @Param("rotationStatus") String rotationStatus);
    
    /**
     * 查询密钥的轮换历史
     */
    @Select("SELECT * FROM key_rotation_history " +
            "WHERE old_key_id = #{keyId} OR new_key_id = #{keyId} " +
            "ORDER BY rotation_time DESC")
    List<KeyRotationHistory> selectRotationHistoryByKeyId(@Param("keyId") Long keyId);
    
    /**
     * 查询最近的轮换记录
     */
    @Select("SELECT * FROM key_rotation_history " +
            "WHERE rotation_status = 'SUCCESS' " +
            "ORDER BY rotation_time DESC LIMIT #{limit}")
    List<KeyRotationHistory> selectRecentRotations(@Param("limit") int limit);
}