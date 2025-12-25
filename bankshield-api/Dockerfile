FROM openjdk:8-jre-slim

# 安装Vault CLI（用于初始化连接）
RUN apt-get update && apt-get install -y wget unzip curl && \
    wget https://releases.hashicorp.com/vault/1.15.0/vault_1.15.0_linux_amd64.zip && \
    unzip vault_1.15.0_linux_amd64.zip && \
    mv vault /usr/local/bin/ && \
    rm vault_1.15.0_linux_amd64.zip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制应用文件
COPY target/bankshield-api-*.jar app.jar
COPY src/main/resources/application*.yml ./config/

# 创建Vault配置目录
RUN mkdir -p /app/vault

# 设置Vault环境变量
ENV VAULT_ADDR=http://vault:8200
ENV VAULT_ROLE_ID=""
ENV VAULT_SECRET_ID=""

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动脚本
COPY scripts/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["java", "-jar", "-Xmx4g", "app.jar"]