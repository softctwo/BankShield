package com.bankshield.gateway.filter;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

/**
 * 缓存请求体的HTTP请求装饰器
 * 用于在签名验证后仍然能够读取请求体
 * 
 * @author BankShield
 */
public class CachedBodyHttpRequestDecorator extends ServerHttpRequestDecorator {
    
    private final String cachedBody;
    private final DataBuffer cachedBuffer;
    
    public CachedBodyHttpRequestDecorator(ServerHttpRequest delegate, String cachedBody, DataBuffer cachedBuffer) {
        super(delegate);
        this.cachedBody = cachedBody;
        this.cachedBuffer = cachedBuffer;
    }
    
    @Override
    public Flux<DataBuffer> getBody() {
        return Flux.just(cachedBuffer);
    }
    
    public String getCachedBody() {
        return cachedBody;
    }
}