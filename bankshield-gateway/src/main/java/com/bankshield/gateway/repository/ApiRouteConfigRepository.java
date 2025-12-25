package com.bankshield.gateway.repository;

import com.bankshield.gateway.entity.ApiRouteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * API路由配置Repository
 * 
 * @author BankShield
 */
@Repository
public interface ApiRouteConfigRepository extends JpaRepository<ApiRouteConfig, Long> {
    
    /**
     * 根据路由ID查询
     */
    Optional<ApiRouteConfig> findByRouteId(String routeId);
    
    /**
     * 根据路由路径查询启用的配置
     */
    Optional<ApiRouteConfig> findByRoutePathAndEnabledTrue(String routePath);
    
    /**
     * 查询所有启用的配置
     */
    List<ApiRouteConfig> findByEnabledTrue();
    
    /**
     * 根据路由ID查询启用的配置
     */
    Optional<ApiRouteConfig> findByRouteIdAndEnabledTrue(String routeId);
    
    /**
     * 统计启用的路由数量
     */
    long countByEnabledTrue();
    
    /**
     * 根据目标服务查询启用的配置
     */
    List<ApiRouteConfig> findByTargetServiceAndEnabledTrue(String targetService);
}