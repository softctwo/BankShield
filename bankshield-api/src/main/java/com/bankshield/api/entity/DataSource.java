package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据源配置实体类
 * 对应数据库表：data_source
 * 用于存储数据源连接配置和扫描状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_source")
public class DataSource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 数据源类型: MYSQL, ORACLE, POSTGRESQL, HDFS, HIVE, HBASE, KAFKA
     */
    private String sourceType;

    /**
     * 连接配置(JSON)
     */
    private String connectionConfig;

    /**
     * 扫描状态: 0:未扫描, 1:扫描中, 2:完成, 3:失败
     */
    private Integer scanStatus;

    /**
     * 最后扫描时间
     */
    @TableField("last_scan_time")
    private LocalDateTime lastScanTime;

    /**
     * 扫描错误信息
     */
    private String scanError;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}