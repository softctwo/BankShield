package com.bankshield.encrypt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.encrypt.entity.EncryptionKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 加密密钥Mapper
 */
@Mapper
public interface EncryptionKeyMapper extends BaseMapper<EncryptionKey> {
    
    /**
     * 分页查询密钥
     */
    IPage<EncryptionKey> selectKeyPage(Page<EncryptionKey> page, 
                                      @Param("keyName") String keyName,
                                      @Param("keyType") String keyType,
                                      @Param("keyStatus") String keyStatus,
                                      @Param("keyUsage") String keyUsage);
    
    /**
     * 查询即将过期的密钥
     */
    @Select("SELECT * FROM encrypt_key WHERE key_status = 'ACTIVE' " +
            "AND expire_time <= #{expireTime} AND deleted = 0")
    List<EncryptionKey> selectExpiringKeys(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 查询需要轮换的密钥
     */
    @Select("SELECT * FROM encrypt_key WHERE key_status = 'ACTIVE' " +
            "AND rotation_cycle IS NOT NULL " +
            "AND (last_rotation_time IS NULL OR last_rotation_time <= #{rotationTime}) " +
            "AND deleted = 0")
    List<EncryptionKey> selectKeysNeedRotation(@Param("rotationTime") LocalDateTime rotationTime);
    
    /**
     * 更新密钥状态
     */
    @Update("UPDATE encrypt_key SET key_status = #{status}, update_time = NOW() WHERE id = #{keyId}")
    int updateKeyStatus(@Param("keyId") Long keyId, @Param("status") String status);
    
    /**
     * 更新轮换信息
     */
    @Update("UPDATE encrypt_key SET last_rotation_time = #{rotationTime}, " +
            "rotation_count = rotation_count + 1, update_time = NOW() WHERE id = #{keyId}")
    int updateRotationInfo(@Param("keyId") Long keyId, @Param("rotationTime") LocalDateTime rotationTime);
    
    /**
     * 统计密钥数量
     */
    @Select("SELECT COUNT(*) FROM encrypt_key WHERE deleted = 0")
    int countAllKeys();
    
    /**
     * 统计活跃密钥数量
     */
    @Select("SELECT COUNT(*) FROM encrypt_key WHERE key_status = 'ACTIVE' AND deleted = 0")
    int countActiveKeys();
    
    /**
     * 统计即将过期的密钥数量
     */
    @Select("SELECT COUNT(*) FROM encrypt_key WHERE key_status = 'ACTIVE' " +
            "AND expire_time <= #{expireTime} AND deleted = 0")
    int countExpiringKeys(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 统计已禁用密钥数量
     */
    @Select("SELECT COUNT(*) FROM encrypt_key WHERE key_status = 'INACTIVE' AND deleted = 0")
    int countInactiveKeys();
}