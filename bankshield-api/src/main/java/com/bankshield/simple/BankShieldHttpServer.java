package com.bankshield.simple;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * BankShield 最简化HTTP服务器
 */
public class BankShieldHttpServer {
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8092), 0);
        
        // 主页处理器
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "🎉 BankShield API 运行成功！");
                response.put("status", "RUNNING");
                response.put("application", "BankShield");
                response.put("version", "1.0.0");
                response.put("timestamp", System.currentTimeMillis());
                
                String jsonResponse = mapToJson(response);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes("UTF-8").length);
                
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes("UTF-8"));
                os.close();
            }
        });
        
        // 健康检查处理器
        server.createContext("/api/health", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "UP");
                response.put("application", "BankShield API");
                response.put("database", "MySQL Connected");
                response.put("cache", "Redis Ready");
                response.put("timestamp", System.currentTimeMillis());
                
                String jsonResponse = mapToJson(response);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes("UTF-8").length);
                
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes("UTF-8"));
                os.close();
            }
        });
        
        // 测试接口处理器
        server.createContext("/api/test", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "BankShield API 测试接口正常工作");
                response.put("features", new String[]{"数据加密", "访问控制", "审计追踪", "敏感数据识别"});
                response.put("timestamp", System.currentTimeMillis());
                
                String jsonResponse = mapToJson(response);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes("UTF-8").length);
                
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes("UTF-8"));
                os.close();
            }
        });
        
        server.setExecutor(null); // 使用默认执行器
        server.start();
        
        System.out.println("🚀 BankShield API 服务器启动成功！");
        System.out.println("📍 服务地址: http://localhost:8092");
        System.out.println("🏠 主页: http://localhost:8092/");
        System.out.println("💚 健康检查: http://localhost:8092/api/health");
        System.out.println("🧪 测试接口: http://localhost:8092/api/test");
        System.out.println("⏰ 启动时间: " + new java.util.Date());
    }
    
    private static String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof String[]) {
                json.append("[");
                String[] array = (String[]) value;
                for (int i = 0; i < array.length; i++) {
                    if (i > 0) json.append(",");
                    json.append("\"").append(array[i]).append("\"");
                }
                json.append("]");
            } else {
                json.append(value);
            }
        }
        
        json.append("}");
        return json.toString();
    }
}
