package com.bankshield.api.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
// import com.fasterxml.jackson.databind.jsontype.impl.BasicPolymorphicTypeValidator; // 注释掉以避免版本兼容性问题
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置类
 * 配置多级缓存策略和不同业务的缓存过期时间
 *
 * @author BankShield
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     * 配置RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 配置序列化器
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 配置key和value的序列化方式
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * 配置CacheManager
     * 支持多级缓存和不同业务的缓存策略
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        
        // 默认缓存配置
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))  // 默认10分钟过期
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // 针对不同缓存名称的特殊配置
        Map<String, RedisCacheConfiguration> initialCacheConfigurations = new HashMap<>();
        
        // 用户缓存 - 30分钟过期
        initialCacheConfigurations.put("userCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 角色权限缓存 - 60分钟过期
        initialCacheConfigurations.put("roleCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(60)));
        
        // 部门缓存 - 30分钟过期
        initialCacheConfigurations.put("deptCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 菜单缓存 - 60分钟过期
        initialCacheConfigurations.put("menuCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(60)));
        
        // 数据字典缓存 - 120分钟过期
        initialCacheConfigurations.put("dictCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(120)));
        
        // 密钥缓存 - 5分钟过期（安全性考虑）
        initialCacheConfigurations.put("keyCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 脱敏规则缓存 - 10分钟过期
        initialCacheConfigurations.put("maskingCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));
        
        // 分类分级缓存 - 30分钟过期
        initialCacheConfigurations.put("classificationCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 审计缓存 - 5分钟过期
        initialCacheConfigurations.put("auditCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 监控数据缓存 - 3分钟过期
        initialCacheConfigurations.put("monitorCache", defaultCacheConfig.entryTtl(Duration.ofMinutes(3)));
        
        return new RedisCacheManager(cacheWriter, defaultCacheConfig, initialCacheConfigurations);
    }

    /**
     * 自定义Redis序列化器
     */
    public static class GenericJackson2JsonRedisSerializer implements RedisSerializer<Object> {
        private final ObjectMapper objectMapper;

        public GenericJackson2JsonRedisSerializer() {
            this.objectMapper = new ObjectMapper();
            this.objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        }

        @Override
        public byte[] serialize(Object t) {
            if (t == null) {
                return new byte[0];
            }
            try {
                return objectMapper.writeValueAsBytes(t);
            } catch (Exception e) {
                throw new RuntimeException("Could not write JSON: " + e.getMessage(), e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return objectMapper.readValue(bytes, Object.class);
            } catch (Exception e) {
                throw new RuntimeException("Could not read JSON: " + e.getMessage(), e);
            }
        }
    }
}