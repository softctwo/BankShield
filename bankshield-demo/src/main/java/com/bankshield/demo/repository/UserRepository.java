package com.bankshield.demo.repository;

import com.bankshield.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据状态查找用户
     */
    List<User> findByStatus(Integer status);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据创建时间范围查询用户
     */
    @Query("SELECT u FROM User u WHERE u.createTime BETWEEN :startTime AND :endTime")
    List<User> findByCreateTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计活跃用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1")
    long countActiveUsers();
}