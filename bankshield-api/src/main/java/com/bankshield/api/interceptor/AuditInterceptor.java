package com.bankshield.api.interceptor;

import com.alibaba.fastjson.JSON;
import com.bankshield.api.entity.OperationAudit;
import com.bankshield.api.service.OperationAuditService;
import com.bankshield.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 审计日志拦截器
 * 拦截所有Controller请求，记录操作日志
 * 
 * @author BankShield
 */
@Slf4j
@Component
public class AuditInterceptor implements HandlerInterceptor {

    @Autowired
    private OperationAuditService operationAuditService;

    private ThreadLocal<OperationAudit> auditThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理Controller方法的请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        try {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String requestUri = request.getRequestURI();
            String method = request.getMethod();

            // 排除审计相关的请求，避免循环记录
            if (requestUri.contains("/api/audit/")) {
                return true;
            }

            // 获取用户信息（这里需要从Session或Token中获取，暂时用默认值）
            Long userId = getCurrentUserId(request);
            String username = getCurrentUsername(request);

            // 获取请求参数
            Map<String, String> params = getRequestParams(request);
            String paramsJson = params.isEmpty() ? "" : JSON.toJSONString(params);

            // 获取IP地址
            String ipAddress = getIpAddress(request);

            // 获取操作模块和操作类型
            String operationModule = getOperationModule(requestUri);
            String operationType = getOperationType(method, requestUri);

            // 创建审计记录
            OperationAudit operationAudit = OperationAudit.builder()
                    .userId(userId)
                    .username(username)
                    .operationType(operationType)
                    .operationModule(operationModule)
                    .operationContent(buildOperationContent(operationType, requestUri, paramsJson))
                    .requestUrl(requestUri)
                    .requestMethod(method)
                    .requestParams(paramsJson)
                    .ipAddress(ipAddress)
                    .location(getLocationByIp(ipAddress))
                    .status(1) // 默认成功，如果失败会在afterCompletion中更新
                    .createTime(LocalDateTime.now())
                    .build();

            auditThreadLocal.set(operationAudit);

        } catch (Exception e) {
            log.error("审计拦截器预处理失败", e);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 不需要处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            OperationAudit operationAudit = auditThreadLocal.get();
            if (operationAudit == null) {
                return;
            }

            // 更新响应结果
            String responseResult = getResponseResult(request);
            operationAudit.setResponseResult(responseResult);

            // 如果有异常，更新状态为失败
            if (ex != null) {
                operationAudit.setStatus(0);
                operationAudit.setErrorMessage(ex.getMessage());
            } else if (response.getStatus() >= 400) {
                // HTTP状态码表示错误
                operationAudit.setStatus(0);
                operationAudit.setErrorMessage("HTTP " + response.getStatus());
            }

            // 异步保存审计记录
            operationAuditService.saveOperationAudit(operationAudit);

        } catch (Exception e) {
            log.error("审计拦截器后处理失败", e);
        } finally {
            auditThreadLocal.remove();
        }
    }

    /**
     * 获取请求参数
     */
    private Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            // 过滤敏感参数
            if (!isSensitiveParam(paramName)) {
                params.put(paramName, paramValue);
            } else {
                params.put(paramName, "***");
            }
        }
        return params;
    }

    /**
     * 判断是否为敏感参数
     */
    private boolean isSensitiveParam(String paramName) {
        String[] sensitiveParams = {"password", "pwd", "token", "secret", "key", "passwd"};
        String lowerParam = paramName.toLowerCase();
        for (String sensitive : sensitiveParams) {
            if (lowerParam.contains(sensitive)) {
                return true;
            }
        }
        return false;
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
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }

    /**
     * 根据IP获取地理位置（简化实现，实际需要调用IP定位服务）
     */
    private String getLocationByIp(String ip) {
        // 这里可以实现调用IP定位服务，如淘宝IP库、百度地图等
        // 暂时返回IP地址
        return ip;
    }

    /**
     * 获取操作模块
     */
    private String getOperationModule(String requestUri) {
        if (requestUri.contains("/user")) {
            return "用户管理";
        } else if (requestUri.contains("/role")) {
            return "角色管理";
        } else if (requestUri.contains("/dept")) {
            return "部门管理";
        } else if (requestUri.contains("/menu")) {
            return "菜单管理";
        } else if (requestUri.contains("/data-asset")) {
            return "数据资产管理";
        } else if (requestUri.contains("/data-source")) {
            return "数据源管理";
        } else if (requestUri.contains("/sensitive-data")) {
            return "敏感数据管理";
        } else if (requestUri.contains("/classification")) {
            return "数据分类分级";
        } else if (requestUri.contains("/encrypt")) {
            return "加密管理";
        } else if (requestUri.contains("/monitor")) {
            return "监控管理";
        } else {
            return "其他模块";
        }
    }

    /**
     * 获取操作类型
     */
    private String getOperationType(String method, String requestUri) {
        if ("GET".equals(method)) {
            if (requestUri.contains("/page") || requestUri.contains("/list")) {
                return "查询";
            } else {
                return "查看详情";
            }
        } else if ("POST".equals(method)) {
            if (requestUri.contains("/login")) {
                return "登录";
            } else if (requestUri.contains("/logout")) {
                return "退出";
            } else {
                return "新增";
            }
        } else if ("PUT".equals(method) || "PATCH".equals(method)) {
            return "修改";
        } else if ("DELETE".equals(method)) {
            return "删除";
        } else {
            return "其他操作";
        }
    }

    /**
     * 构建操作内容描述
     */
    private String buildOperationContent(String operationType, String requestUri, String paramsJson) {
        StringBuilder content = new StringBuilder();
        content.append(operationType);
        if (requestUri.contains("/user")) {
            content.append("用户");
        } else if (requestUri.contains("/role")) {
            content.append("角色");
        } else if (requestUri.contains("/dept")) {
            content.append("部门");
        } else if (requestUri.contains("/menu")) {
            content.append("菜单");
        } else if (requestUri.contains("/data-asset")) {
            content.append("数据资产");
        } else if (requestUri.contains("/data-source")) {
            content.append("数据源");
        } else if (requestUri.contains("/sensitive-data")) {
            content.append("敏感数据");
        } else if (requestUri.contains("/classification")) {
            content.append("分类分级");
        } else if (requestUri.contains("/encrypt")) {
            content.append("加密配置");
        } else if (requestUri.contains("/monitor")) {
            content.append("监控配置");
        } else {
            content.append("系统资源");
        }
        
        if (!paramsJson.isEmpty() && paramsJson.length() < 100) {
            content.append("，参数：").append(paramsJson);
        }
        
        return content.toString();
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                // 如果是字符串类型且为数字，则转换为Long
                if (principal instanceof String) {
                    try {
                        return Long.parseLong((String) principal);
                    } catch (NumberFormatException e) {
                        log.warn("无法将用户标识符转换为用户ID: {}", principal);
                    }
                }

                // 如果是自定义的UserDetails对象，需要根据实际情况获取ID
                // 例如：return ((UserDetails) principal).getUserId();
            }

            // 如果获取失败，返回匿名用户ID
            log.warn("无法从认证信息中获取用户ID，使用默认值");
            return null;
        } catch (Exception e) {
            log.error("获取当前用户ID时发生错误: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                // 如果principal是字符串，则直接返回
                if (principal instanceof String) {
                    return (String) principal;
                }

                // 如果是自定义的UserDetails对象，需要根据实际情况获取用户名
                // 例如：return ((UserDetails) principal).getUsername();
            }

            // 如果获取失败，返回匿名用户名
            log.warn("无法从认证信息中获取用户名，使用默认值");
            return "anonymous";
        } catch (Exception e) {
            log.error("获取当前用户名时发生错误: {}", e.getMessage());
            return "anonymous";
        }
    }

    /**
     * 获取响应结果
     */
    private String getResponseResult(HttpServletRequest request) {
        // 这里可以从请求属性中获取响应结果，需要在Controller中设置
        Object result = request.getAttribute("responseResult");
        if (result != null) {
            try {
                String resultStr = JSON.toJSONString(result);
                // 限制响应结果长度，避免日志过大
                if (resultStr.length() > 500) {
                    return resultStr.substring(0, 500) + "...";
                }
                return resultStr;
            } catch (Exception e) {
                log.error("序列化响应结果失败", e);
            }
        }
        return "";
    }
}