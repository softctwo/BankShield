package com.bankshield.api.mapper;

import com.bankshield.api.entity.SecurityKey;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 安全密钥Mapper接口
 *
 * @author BankShield
 */
@Mapper
public interface SecurityKeyMapper extends BaseMapper<SecurityKey> {

    /**
     * 根据密钥类型查询密钥列表
     *
     * @param keyType 密钥类型
     * @return 密钥列表
     */
    @Select("SELECT * FROM security_key WHERE key_type = #{keyType} AND deleted = 0")
    List<SecurityKey> selectByKeyType(@Param("keyType") String keyType);

    /**
     * 根据状态查询密钥列表
     *
     * @param status 状态
     * @return 密钥列表
     */
    @Select("SELECT * FROM security_key WHERE status = #{status} AND deleted = 0")
    List<SecurityKey> selectByStatus(@Param("status") Integer status);

    /**
     * 查询即将过期的密钥列表
     *
     * @param days 天数
     * @return 密钥列表
     */
    @Select("SELECT * FROM security_key WHERE expire_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) AND status = 1 AND deleted = 0")
    List<SecurityKey> selectExpiringSoon(@Param("days") int days);

    /**
     * 查询已过期的密钥列表
     *
     * @return 密钥列表
     */
    @Select("SELECT * FROM security_key WHERE expire_time < NOW() AND status = 1 AND deleted = 0")
    List<SecurityKey> selectExpired();

    /**
     * 根据数据源ID查询密钥列表
     *
     * @param dataSourceId 数据源ID
     * @return 密钥列表
     */
    @Select("SELECT * FROM security_key WHERE data_source_id = #{dataSourceId} AND deleted = 0")
    List<SecurityKey> selectByDataSourceId(@Param("dataSourceId") Long dataSourceId);

    /**
     * 查询常见密钥类型
     *
     * @return 密钥类型列表
     */
    @Select("SELECT DISTINCT key_type FROM security_key WHERE deleted = 0 ORDER BY key_type")
    List<String> selectCommonKeyTypes();
}
