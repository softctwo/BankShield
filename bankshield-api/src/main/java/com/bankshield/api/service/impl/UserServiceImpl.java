package com.bankshield.api.service.impl;

import com.bankshield.api.entity.User;
import com.bankshield.api.mapper.UserMapper;
import com.bankshield.api.service.LoginAuditService;
import com.bankshield.api.service.UserService;
import com.bankshield.common.result.Result;
import com.bankshield.common.crypto.EncryptUtil;
import com.bankshield.common.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginAuditService loginAuditService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result<User> getUserById(Long id) {
        try {
            User user = this.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            log.error("查询用户失败: {}", e.getMessage());
            return Result.error("查询用户失败");
        }
    }

    @Override
    public Result<Page<User>> getUserPage(int page, int size, String username, Long deptId) {
        try {
            Page<User> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            
            // 添加查询条件
            if (StringUtils.hasText(username)) {
                queryWrapper.like(User::getUsername, username);
            }
            if (deptId != null) {
                queryWrapper.eq(User::getDeptId, deptId);
            }
            
            // 按创建时间降序排序
            queryWrapper.orderByDesc(User::getCreateTime);
            
            Page<User> userPage = this.page(pageParam, queryWrapper);
            return Result.success(userPage);
        } catch (Exception e) {
            log.error("分页查询用户失败: {}", e.getMessage());
            return Result.error("分页查询用户失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addUser(User user) {
        try {
            // 参数校验
            if (user == null) {
                return Result.error("用户信息不能为空");
            }
            if (!StringUtils.hasText(user.getUsername())) {
                return Result.error("用户名不能为空");
            }
            if (!StringUtils.hasText(user.getPassword())) {
                return Result.error("密码不能为空");
            }
            if (!StringUtils.hasText(user.getName())) {
                return Result.error("真实姓名不能为空");
            }

            // 检查用户名是否已存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, user.getUsername());
            if (this.count(queryWrapper) > 0) {
                return Result.error("用户名已存在");
            }

            // 设置默认值
            if (user.getStatus() == null) {
                user.setStatus(1); // 默认启用
            }
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            
            // 设置创建人（这里应该从当前登录用户获取，暂时使用系统用户）
            String currentUser = "system";
            user.setCreateBy(currentUser);
            user.setUpdateBy(currentUser);

            // BCrypt密码加密
            String encryptedPassword = EncryptUtil.bcryptEncrypt(user.getPassword());
            user.setPassword(encryptedPassword);

            // 保存用户
            boolean result = this.save(user);
            if (result) {
                return Result.success("添加用户成功");
            } else {
                return Result.error("添加用户失败");
            }
        } catch (Exception e) {
            log.error("添加用户失败: {}", e.getMessage());
            return Result.error("添加用户失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateUser(User user) {
        try {
            // 参数校验
            if (user == null || user.getId() == null) {
                return Result.error("用户ID不能为空");
            }

            // 检查用户是否存在
            User existingUser = this.getById(user.getId());
            if (existingUser == null) {
                return Result.error("用户不存在");
            }

            // 如果修改了用户名，检查新用户名是否已存在
            if (StringUtils.hasText(user.getUsername()) && !user.getUsername().equals(existingUser.getUsername())) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getUsername, user.getUsername());
                if (this.count(queryWrapper) > 0) {
                    return Result.error("用户名已存在");
                }
            }

            // 设置更新时间
            user.setUpdateTime(LocalDateTime.now());
            user.setUpdateBy("system"); // 应该从当前登录用户获取

            // 如果密码不为空，需要重新加密
            if (StringUtils.hasText(user.getPassword())) {
                String encryptedPassword = EncryptUtil.bcryptEncrypt(user.getPassword());
                user.setPassword(encryptedPassword);
            } else {
                // 如果密码为空，保持原密码不变
                user.setPassword(null); // MyBatis-Plus不会更新null字段
            }

            // 更新用户
            boolean result = this.updateById(user);
            if (result) {
                return Result.success("更新用户成功");
            } else {
                return Result.error("更新用户失败");
            }
        } catch (Exception e) {
            log.error("更新用户失败: {}", e.getMessage());
            return Result.error("更新用户失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteUser(Long id) {
        try {
            if (id == null) {
                return Result.error("用户ID不能为空");
            }

            // 检查用户是否存在
            User user = this.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 删除用户
            boolean result = this.removeById(id);
            if (result) {
                return Result.success("删除用户成功");
            } else {
                return Result.error("删除用户失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            return Result.error("删除用户失败");
        }
    }

    @Override
    public Result<Map<String, Object>> login(String username, String password) {
        try {
            // 参数校验
            if (!StringUtils.hasText(username)) {
                return Result.error("用户名不能为空");
            }
            if (!StringUtils.hasText(password)) {
                return Result.error("密码不能为空");
            }

            // 查询用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, username);
            User user = this.getOne(queryWrapper);

            // 获取请求信息
            HttpServletRequest request = null;
            String loginIp = "127.0.0.1";
            String browser = "Unknown";
            String os = "Unknown";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    request = attributes.getRequest();
                    loginIp = getIpAddress(request);
                    browser = getBrowserInfo(request);
                    os = getOSInfo(request);
                }
            } catch (Exception e) {
                log.warn("获取请求信息失败: {}", e.getMessage());
            }

            if (user == null) {
                // 记录登录失败审计
                loginAuditService.recordLoginFailure(username, loginIp, loginIp, browser, os, "用户名不存在");
                return Result.error("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() == null || user.getStatus() == 0) {
                // 记录登录失败审计
                loginAuditService.recordLoginFailure(username, loginIp, loginIp, browser, os, "用户已被禁用");
                return Result.error("用户已被禁用");
            }

            // 验证密码
            boolean isPasswordValid = EncryptUtil.bcryptCheck(password, user.getPassword());
            if (!isPasswordValid) {
                // 记录登录失败审计
                loginAuditService.recordLoginFailure(username, loginIp, loginIp, browser, os, "密码错误");
                return Result.error("用户名或密码错误");
            }

            // 记录登录成功审计
            loginAuditService.recordLoginSuccess(user.getId(), username, loginIp, loginIp, browser, os);

            // 获取用户权限（角色编码）
            List<String> authorities = userMapper.selectRoleCodesByUserId(user.getId());

            // 生成JWT token
            String token = jwtUtil.generateToken(user.getId(), username, authorities);

            // 构建返回数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("token", token);
            resultData.put("userId", user.getId());
            resultData.put("username", username);
            resultData.put("name", user.getName());
            resultData.put("message", "登录成功");

            // 返回成功结果
            return Result.<Map<String, Object>>success(resultData, "登录成功");
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            return Result.error("登录失败");
        }
    }
    
    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
    
    /**
     * 获取浏览器信息
     */
    private String getBrowserInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "Unknown";
        
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("Opera")) return "Opera";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "IE";
        
        return "Unknown";
    }
    
    /**
     * 获取操作系统信息
     */
    private String getOSInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "Unknown";
        
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS X")) return "Mac OS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iOS")) return "iOS";
        
        return "Unknown";
    }
}