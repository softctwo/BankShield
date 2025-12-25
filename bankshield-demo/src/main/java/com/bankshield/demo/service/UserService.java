package com.bankshield.demo.service;

import com.bankshield.demo.entity.User;
import com.bankshield.demo.repository.UserRepository;
import com.bankshield.demo.crypto.SM3Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务层
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 创建用户
     */
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + user.getEmail());
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已存在: " + user.getPhone());
        }

        // 密码加密（使用SM3加盐哈希）
        if (user.getPassword() != null) {
            byte[] salt = SM3Util.generateSalt(16);
            String hashedPassword = SM3Util.hashWithSalt(user.getPassword(), salt);
            user.setPassword(hashedPassword);
        }

        return userRepository.save(user);
    }

    /**
     * 根据ID查找用户
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 根据用户名查找用户
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 用户登录验证
     */
    @Transactional(readOnly = true)
    public boolean validateUser(String username, String password) {
        Optional<User> userOptional = findByUsername(username);
        if (!userOptional.isPresent()) {
            return false;
        }

        User user = userOptional.get();
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            return false;
        }

        // 验证密码
        return SM3Util.verifyWithSalt(password, user.getPassword());
    }

    /**
     * 更新用户
     */
    public User updateUser(Long id, User userUpdate) {
        Optional<User> existingUserOptional = userRepository.findById(id);
        if (!existingUserOptional.isPresent()) {
            throw new RuntimeException("用户不存在: " + id);
        }

        User existingUser = existingUserOptional.get();

        // 检查用户名是否已被其他用户使用
        if (!existingUser.getUsername().equals(userUpdate.getUsername()) && 
            userRepository.existsByUsername(userUpdate.getUsername())) {
            throw new RuntimeException("用户名已存在: " + userUpdate.getUsername());
        }

        // 检查邮箱是否已被其他用户使用
        if (userUpdate.getEmail() != null && 
            !userUpdate.getEmail().equals(existingUser.getEmail()) &&
            userRepository.existsByEmail(userUpdate.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + userUpdate.getEmail());
        }

        // 更新用户信息
        existingUser.setUsername(userUpdate.getUsername());
        existingUser.setEmail(userUpdate.getEmail());
        existingUser.setPhone(userUpdate.getPhone());
        existingUser.setRealName(userUpdate.getRealName());
        existingUser.setStatus(userUpdate.getStatus());

        return userRepository.save(existingUser);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户不存在: " + id);
        }

        userRepository.deleteById(id);
    }

    /**
     * 获取所有用户
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * 获取活跃用户列表
     */
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        return userRepository.findByStatus(1);
    }

    /**
     * 统计活跃用户数量
     */
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }

    /**
     * 获取系统统计信息
     */
    @Transactional(readOnly = true)
    public Object getSystemStats() {
        long totalUsers = userRepository.count();
        long activeUsers = countActiveUsers();
        
        return new Object() {
            public long getTotalUsers() { return totalUsers; }
            public long getActiveUsers() { return activeUsers; }
            public long getInactiveUsers() { return totalUsers - activeUsers; }
        };
    }
}