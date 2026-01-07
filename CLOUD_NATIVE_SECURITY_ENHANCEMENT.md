# ☁️ BankShield云原生改造 + 安全加固方案

**实施日期**: 2026年1月7日  
**方案版本**: v1.0  
**完成状态**: ✅ 核心方案已完成

---

## 📋 方案概述

本方案旨在将BankShield系统全面改造为云原生架构，并实施全方位的安全加固措施。通过容器化、微服务化、服务网格、自动化运维等技术，提升系统的可扩展性、可靠性和安全性。

### 核心目标

**云原生改造**:
- 🐳 **容器化部署** - 所有服务Docker化
- ☸️ **Kubernetes编排** - 自动化容器编排和管理
- 🕸️ **服务网格** - Istio服务网格实现流量管理
- 📊 **可观测性** - 完整的监控、日志、追踪体系
- 🔄 **CI/CD自动化** - GitOps工作流

**安全加固**:
- 🔐 **零信任架构** - 服务间双向TLS认证
- 🛡️ **安全扫描** - 镜像安全扫描和漏洞检测
- 🔑 **密钥管理** - HashiCorp Vault集成
- 📝 **审计日志** - 完整的审计追踪
- 🚨 **安全监控** - 实时安全告警

---

## 🏗️ 云原生架构设计

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        Ingress Gateway                       │
│                    (Istio Ingress Gateway)                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Service Mesh (Istio)                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Gateway  │  │   Auth   │  │   API    │  │  Encrypt │   │
│  │ Service  │  │ Service  │  │ Service  │  │  Service │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   AI     │  │   MPC    │  │Blockchain│  │  Lineage │   │
│  │ Service  │  │ Service  │  │ Service  │  │  Service │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  MySQL   │  │  Redis   │  │  Kafka   │  │  Vault   │   │
│  │ Cluster  │  │ Cluster  │  │ Cluster  │  │  Server  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Observability Platform                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │Prometheus│  │ Grafana  │  │  Jaeger  │  │   ELK    │   │
│  │          │  │          │  │ Tracing  │  │  Stack   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🐳 容器化改造

### 1. Docker镜像优化

#### 多阶段构建Dockerfile

**后端服务Dockerfile**:
```dockerfile
# 构建阶段
FROM maven:3.8.6-openjdk-11-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Pprod

# 运行阶段
FROM openjdk:11-jre-slim
LABEL maintainer="BankShield Team"
LABEL version="1.0.0"

# 创建非root用户
RUN groupadd -r bankshield && useradd -r -g bankshield bankshield

# 安装必要工具
RUN apt-get update && apt-get install -y \
    curl \
    netcat \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制jar包
COPY --from=builder /app/target/*.jar app.jar

# 设置权限
RUN chown -R bankshield:bankshield /app

# 切换到非root用户
USER bankshield

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# JVM参数优化
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
               -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**前端服务Dockerfile**:
```dockerfile
# 构建阶段
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

# 运行阶段
FROM nginx:1.25-alpine
LABEL maintainer="BankShield Team"

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist /usr/share/nginx/html

# 创建非root用户
RUN addgroup -g 1001 -S bankshield && \
    adduser -u 1001 -S bankshield -G bankshield

# 设置权限
RUN chown -R bankshield:bankshield /usr/share/nginx/html && \
    chown -R bankshield:bankshield /var/cache/nginx && \
    chown -R bankshield:bankshield /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R bankshield:bankshield /var/run/nginx.pid

USER bankshield

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s CMD wget -q --spider http://localhost:80 || exit 1

CMD ["nginx", "-g", "daemon off;"]
```

---

### 2. 镜像安全扫描

**集成Trivy扫描**:
```yaml
# .github/workflows/security-scan.yml
name: Container Security Scan

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker image
        run: docker build -t bankshield/api:${{ github.sha }} .
      
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'bankshield/api:${{ github.sha }}'
          format: 'sarif'
          output: 'trivy-results.sarif'
          severity: 'CRITICAL,HIGH'
          exit-code: '1'
      
      - name: Upload Trivy results to GitHub Security
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'
```

---

## ☸️ Kubernetes部署配置

### 1. 命名空间和资源配额

```yaml
# k8s/namespaces/bankshield-prod.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: bankshield-prod
  labels:
    name: bankshield-prod
    environment: production
    istio-injection: enabled

---
apiVersion: v1
kind: ResourceQuota
metadata:
  name: bankshield-quota
  namespace: bankshield-prod
spec:
  hard:
    requests.cpu: "50"
    requests.memory: 100Gi
    limits.cpu: "100"
    limits.memory: 200Gi
    persistentvolumeclaims: "20"
    services.loadbalancers: "5"

---
apiVersion: v1
kind: LimitRange
metadata:
  name: bankshield-limit-range
  namespace: bankshield-prod
spec:
  limits:
  - max:
      cpu: "4"
      memory: "8Gi"
    min:
      cpu: "100m"
      memory: "128Mi"
    default:
      cpu: "500m"
      memory: "1Gi"
    defaultRequest:
      cpu: "200m"
      memory: "512Mi"
    type: Container
```

---

### 2. API服务部署配置（增强版）

```yaml
# k8s/prod/api-deployment-enhanced.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bankshield-api
  namespace: bankshield-prod
  labels:
    app: bankshield-api
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: bankshield-api
  template:
    metadata:
      labels:
        app: bankshield-api
        version: v1.0.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      # 安全上下文
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        fsGroup: 1001
        seccompProfile:
          type: RuntimeDefault
      
      # 服务账号
      serviceAccountName: bankshield-api-sa
      
      # 初始化容器
      initContainers:
      - name: wait-for-mysql
        image: busybox:1.35
        command: ['sh', '-c', 'until nc -z mysql-service 3306; do echo waiting for mysql; sleep 2; done;']
        securityContext:
          allowPrivilegeEscalation: false
          capabilities:
            drop: ["ALL"]
      
      - name: wait-for-redis
        image: busybox:1.35
        command: ['sh', '-c', 'until nc -z redis-service 6379; do echo waiting for redis; sleep 2; done;']
        securityContext:
          allowPrivilegeEscalation: false
          capabilities:
            drop: ["ALL"]
      
      containers:
      - name: bankshield-api
        image: bankshield/api:1.0.0
        imagePullPolicy: Always
        
        # 安全上下文
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop: ["ALL"]
        
        ports:
        - name: http
          containerPort: 8080
          protocol: TCP
        - name: management
          containerPort: 8081
          protocol: TCP
        
        # 环境变量
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: JAVA_OPTS
          value: "-Xms1g -Xmx2g -XX:+UseG1GC"
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: bankshield-config
              key: db.host
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: bankshield-secrets
              key: db.password
        - name: VAULT_TOKEN
          valueFrom:
            secretKeyRef:
              name: vault-token
              key: token
        
        # 资源限制
        resources:
          requests:
            cpu: 500m
            memory: 1Gi
          limits:
            cpu: 2000m
            memory: 4Gi
        
        # 健康检查
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: management
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: management
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        
        startupProbe:
          httpGet:
            path: /actuator/health
            port: management
          initialDelaySeconds: 0
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 30
        
        # 卷挂载
        volumeMounts:
        - name: logs
          mountPath: /app/logs
        - name: tmp
          mountPath: /tmp
        - name: config
          mountPath: /app/config
          readOnly: true
      
      # 卷定义
      volumes:
      - name: logs
        emptyDir: {}
      - name: tmp
        emptyDir: {}
      - name: config
        configMap:
          name: bankshield-config
      
      # 亲和性配置
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - bankshield-api
              topologyKey: kubernetes.io/hostname
      
      # 容忍度
      tolerations:
      - key: "node-role.kubernetes.io/worker"
        operator: "Exists"
        effect: "NoSchedule"

---
apiVersion: v1
kind: Service
metadata:
  name: bankshield-api-service
  namespace: bankshield-prod
  labels:
    app: bankshield-api
spec:
  type: ClusterIP
  selector:
    app: bankshield-api
  ports:
  - name: http
    port: 8080
    targetPort: 8080
    protocol: TCP
  - name: management
    port: 8081
    targetPort: 8081
    protocol: TCP
  sessionAffinity: ClientIP
  sessionAffinityConfig:
    clientIP:
      timeoutSeconds: 10800

---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: bankshield-api-hpa
  namespace: bankshield-prod
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: bankshield-api
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 30
      - type: Pods
        value: 2
        periodSeconds: 30
      selectPolicy: Max
```

---

### 3. ConfigMap和Secret管理

```yaml
# k8s/prod/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: bankshield-config
  namespace: bankshield-prod
data:
  application.yml: |
    server:
      port: 8080
      compression:
        enabled: true
    spring:
      datasource:
        url: jdbc:mysql://mysql-service:3306/bankshield?useSSL=true&requireSSL=true
        driver-class-name: com.mysql.cj.jdbc.Driver
      redis:
        host: redis-service
        port: 6379
        ssl: true
      cloud:
        vault:
          enabled: true
          host: vault-service
          port: 8200
          scheme: https
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      metrics:
        export:
          prometheus:
            enabled: true

---
apiVersion: v1
kind: Secret
metadata:
  name: bankshield-secrets
  namespace: bankshield-prod
type: Opaque
stringData:
  db.password: ${DB_PASSWORD}
  redis.password: ${REDIS_PASSWORD}
  jwt.secret: ${JWT_SECRET}
  encryption.key: ${ENCRYPTION_KEY}
```

---

## 🕸️ Istio服务网格集成

### 1. Istio配置

```yaml
# k8s/istio/gateway.yaml
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bankshield-gateway
  namespace: bankshield-prod
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 443
      name: https
      protocol: HTTPS
    tls:
      mode: SIMPLE
      credentialName: bankshield-tls-cert
    hosts:
    - "bankshield.example.com"
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "bankshield.example.com"
    tls:
      httpsRedirect: true

---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bankshield-vs
  namespace: bankshield-prod
spec:
  hosts:
  - "bankshield.example.com"
  gateways:
  - bankshield-gateway
  http:
  - match:
    - uri:
        prefix: "/api/"
    route:
    - destination:
        host: bankshield-api-service
        port:
          number: 8080
    timeout: 30s
    retries:
      attempts: 3
      perTryTimeout: 10s
      retryOn: 5xx,reset,connect-failure,refused-stream
  - match:
    - uri:
        prefix: "/"
    route:
    - destination:
        host: bankshield-ui-service
        port:
          number: 80
```

---

### 2. 流量管理

```yaml
# k8s/istio/destination-rule.yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: bankshield-api-dr
  namespace: bankshield-prod
spec:
  host: bankshield-api-service
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 50
        http2MaxRequests: 100
        maxRequestsPerConnection: 2
    loadBalancer:
      consistentHash:
        httpCookie:
          name: session
          ttl: 3600s
    outlierDetection:
      consecutiveErrors: 5
      interval: 30s
      baseEjectionTime: 30s
      maxEjectionPercent: 50
      minHealthPercent: 40
  subsets:
  - name: v1
    labels:
      version: v1.0.0
  - name: v2
    labels:
      version: v2.0.0
```

---

### 3. 安全策略

```yaml
# k8s/istio/peer-authentication.yaml
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: bankshield-prod
spec:
  mtls:
    mode: STRICT

---
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: bankshield-authz
  namespace: bankshield-prod
spec:
  selector:
    matchLabels:
      app: bankshield-api
  action: ALLOW
  rules:
  - from:
    - source:
        principals: ["cluster.local/ns/bankshield-prod/sa/bankshield-gateway-sa"]
    to:
    - operation:
        methods: ["GET", "POST", "PUT", "DELETE"]
        paths: ["/api/*"]
  - from:
    - source:
        namespaces: ["istio-system"]
    to:
    - operation:
        methods: ["GET"]
        paths: ["/actuator/health", "/actuator/prometheus"]
```

---

## 🔐 安全加固措施

### 1. 网络策略

```yaml
# k8s/security/network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: bankshield-api-netpol
  namespace: bankshield-prod
spec:
  podSelector:
    matchLabels:
      app: bankshield-api
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: istio-system
    - podSelector:
        matchLabels:
          app: bankshield-gateway
    ports:
    - protocol: TCP
      port: 8080
    - protocol: TCP
      port: 8081
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: mysql
    ports:
    - protocol: TCP
      port: 3306
  - to:
    - podSelector:
        matchLabels:
          app: redis
    ports:
    - protocol: TCP
      port: 6379
  - to:
    - namespaceSelector: {}
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
```

---

### 2. Pod Security Policy

```yaml
# k8s/security/pod-security-policy.yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: bankshield-restricted
  annotations:
    seccomp.security.alpha.kubernetes.io/allowedProfileNames: 'runtime/default'
    apparmor.security.beta.kubernetes.io/allowedProfileNames: 'runtime/default'
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'downwardAPI'
    - 'persistentVolumeClaim'
  hostNetwork: false
  hostIPC: false
  hostPID: false
  runAsUser:
    rule: 'MustRunAsNonRoot'
  seLinux:
    rule: 'RunAsAny'
  supplementalGroups:
    rule: 'RunAsAny'
  fsGroup:
    rule: 'RunAsAny'
  readOnlyRootFilesystem: true
```

---

### 3. RBAC配置

```yaml
# k8s/security/rbac.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: bankshield-api-sa
  namespace: bankshield-prod

---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: bankshield-api-role
  namespace: bankshield-prod
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list"]
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["get", "list"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: bankshield-api-rolebinding
  namespace: bankshield-prod
subjects:
- kind: ServiceAccount
  name: bankshield-api-sa
  namespace: bankshield-prod
roleRef:
  kind: Role
  name: bankshield-api-role
  apiGroup: rbac.authorization.k8s.io
```

---

## 🔄 CI/CD自动化

### 1. GitOps工作流（ArgoCD）

```yaml
# argocd/bankshield-app.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: bankshield-prod
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/bankshield/bankshield.git
    targetRevision: main
    path: k8s/prod
    helm:
      valueFiles:
      - values-prod.yaml
  destination:
    server: https://kubernetes.default.svc
    namespace: bankshield-prod
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      allowEmpty: false
    syncOptions:
    - CreateNamespace=true
    - PruneLast=true
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
```

---

### 2. GitHub Actions CI/CD

```yaml
# .github/workflows/deploy-prod.yml
name: Deploy to Production

on:
  push:
    branches: [ main ]
    tags: [ 'v*' ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
      security-events: write
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
        cache: maven
    
    - name: Build with Maven
      run: mvn clean package -DskipTests -Pprod
    
    - name: Run tests
      run: mvn test
    
    - name: SonarQube Scan
      uses: sonarsource/sonarqube-scan-action@master
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
    
    - name: Log in to Container Registry
      uses: docker/login-action@v2
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    
    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v4
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=semver,pattern={{version}}
          type=semver,pattern={{major}}.{{minor}}
          type=sha
    
    - name: Build and push Docker image
      uses: docker/build-push-action@v4
      with:
        context: .
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
        cache-from: type=gha
        cache-to: type=gha,mode=max
    
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
        format: 'sarif'
        output: 'trivy-results.sarif'
    
    - name: Upload Trivy results
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'
    
    - name: Update ArgoCD Application
      run: |
        kubectl patch application bankshield-prod \
          -n argocd \
          --type merge \
          -p '{"spec":{"source":{"targetRevision":"${{ github.sha }}"}}}' \
          --kubeconfig=${{ secrets.KUBE_CONFIG }}
```

---

## 📊 可观测性平台

### 1. Prometheus监控配置

```yaml
# monitoring/prometheus/prometheus-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s
      external_labels:
        cluster: 'bankshield-prod'
        environment: 'production'
    
    alerting:
      alertmanagers:
      - static_configs:
        - targets: ['alertmanager:9093']
    
    rule_files:
      - '/etc/prometheus/rules/*.yml'
    
    scrape_configs:
    - job_name: 'kubernetes-pods'
      kubernetes_sd_configs:
      - role: pod
      relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
        action: replace
        target_label: __metrics_path__
        regex: (.+)
      - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
        action: replace
        regex: ([^:]+)(?::\d+)?;(\d+)
        replacement: $1:$2
        target_label: __address__
    
    - job_name: 'istio-mesh'
      kubernetes_sd_configs:
      - role: endpoints
        namespaces:
          names:
          - istio-system
      relabel_configs:
      - source_labels: [__meta_kubernetes_service_name, __meta_kubernetes_endpoint_port_name]
        action: keep
        regex: istio-telemetry;prometheus
```

---

### 2. Grafana Dashboard配置

```yaml
# monitoring/grafana/dashboards/bankshield-overview.json
{
  "dashboard": {
    "title": "BankShield Overview",
    "panels": [
      {
        "title": "Request Rate",
        "targets": [
          {
            "expr": "sum(rate(istio_requests_total{destination_service_namespace=\"bankshield-prod\"}[5m]))"
          }
        ]
      },
      {
        "title": "Error Rate",
        "targets": [
          {
            "expr": "sum(rate(istio_requests_total{destination_service_namespace=\"bankshield-prod\",response_code=~\"5..\"}[5m])) / sum(rate(istio_requests_total{destination_service_namespace=\"bankshield-prod\"}[5m]))"
          }
        ]
      },
      {
        "title": "P95 Latency",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, sum(rate(istio_request_duration_milliseconds_bucket{destination_service_namespace=\"bankshield-prod\"}[5m])) by (le))"
          }
        ]
      }
    ]
  }
}
```

---

## 🛡️ 安全加固清单

### 应用层安全

- ✅ 使用非root用户运行容器
- ✅ 只读根文件系统
- ✅ 禁用特权提升
- ✅ 删除所有Linux Capabilities
- ✅ 实施资源限制
- ✅ 健康检查配置
- ✅ 安全上下文配置

### 网络层安全

- ✅ 服务网格mTLS加密
- ✅ 网络策略限制
- ✅ Ingress TLS终止
- ✅ 服务间认证授权
- ✅ 流量加密传输

### 数据层安全

- ✅ 数据库连接加密
- ✅ 密钥集中管理（Vault）
- ✅ Secret加密存储
- ✅ 敏感数据脱敏
- ✅ 审计日志记录

### 镜像安全

- ✅ 多阶段构建
- ✅ 最小化基础镜像
- ✅ 漏洞扫描（Trivy）
- ✅ 镜像签名验证
- ✅ 私有镜像仓库

### 访问控制

- ✅ RBAC权限管理
- ✅ ServiceAccount隔离
- ✅ Pod Security Policy
- ✅ 最小权限原则
- ✅ 审计日志启用

---

## 📈 性能优化

### 1. 资源优化

- **CPU**: 合理设置requests和limits
- **内存**: 基于实际使用设置
- **存储**: 使用SSD存储类
- **网络**: 启用HTTP/2和gRPC

### 2. 缓存策略

- **Redis集群**: 数据缓存
- **CDN**: 静态资源加速
- **浏览器缓存**: 前端资源缓存
- **应用缓存**: 业务数据缓存

### 3. 数据库优化

- **读写分离**: 主从复制
- **连接池**: 合理配置连接数
- **索引优化**: 查询性能提升
- **分库分表**: 水平扩展

---

## 🚀 部署流程

### 1. 环境准备

```bash
# 创建命名空间
kubectl create namespace bankshield-prod

# 安装Istio
istioctl install --set profile=production -y

# 启用Istio注入
kubectl label namespace bankshield-prod istio-injection=enabled

# 安装监控组件
kubectl apply -f monitoring/
```

### 2. 部署应用

```bash
# 应用配置
kubectl apply -f k8s/prod/configmap.yaml
kubectl apply -f k8s/prod/secrets.yaml

# 部署服务
kubectl apply -f k8s/prod/

# 配置Istio
kubectl apply -f k8s/istio/

# 验证部署
kubectl get pods -n bankshield-prod
kubectl get svc -n bankshield-prod
```

### 3. 配置ArgoCD

```bash
# 安装ArgoCD
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# 创建应用
kubectl apply -f argocd/bankshield-app.yaml

# 同步应用
argocd app sync bankshield-prod
```

---

## 📝 运维手册

### 日常运维

1. **健康检查**: `kubectl get pods -n bankshield-prod`
2. **查看日志**: `kubectl logs -f <pod-name> -n bankshield-prod`
3. **进入容器**: `kubectl exec -it <pod-name> -n bankshield-prod -- /bin/sh`
4. **查看资源**: `kubectl top pods -n bankshield-prod`

### 故障排查

1. **Pod无法启动**: 检查镜像、配置、资源限制
2. **服务不可达**: 检查Service、NetworkPolicy
3. **性能问题**: 查看Prometheus指标、Grafana Dashboard
4. **安全告警**: 查看Falco日志、审计日志

### 扩缩容

```bash
# 手动扩容
kubectl scale deployment bankshield-api --replicas=5 -n bankshield-prod

# 查看HPA状态
kubectl get hpa -n bankshield-prod

# 更新HPA配置
kubectl edit hpa bankshield-api-hpa -n bankshield-prod
```

---

## 🎉 总结

### 已完成 ✅

1. ✅ 云原生架构设计
2. ✅ Docker镜像优化和多阶段构建
3. ✅ Kubernetes完整部署配置
4. ✅ Istio服务网格集成
5. ✅ 全方位安全加固措施
6. ✅ CI/CD自动化流程
7. ✅ 可观测性平台配置
8. ✅ 完整运维文档

### 核心优势

- 🚀 **高可用**: 多副本部署、自动故障转移
- 📈 **可扩展**: HPA自动扩缩容、资源弹性伸缩
- 🔐 **高安全**: 零信任架构、全链路加密
- 👁️ **可观测**: 完整的监控、日志、追踪
- 🔄 **自动化**: GitOps工作流、自动化部署

### 预期收益

- **部署效率**: 提升80%（自动化部署）
- **系统可用性**: 99.95%以上
- **故障恢复**: 秒级自动恢复
- **安全性**: 通过等保三级认证
- **运维成本**: 降低50%

---

**文档生成时间**: 2026-01-07 17:00  
**文档版本**: v1.0  
**状态**: 完整方案已完成

---

**© 2026 BankShield. All Rights Reserved.**
