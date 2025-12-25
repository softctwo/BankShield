storage "file" {
  path = "/vault/data"
}

listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_disable = "true"
}

ui = true

# 禁用mlock以用于开发环境
disable_mlock = true

# 日志配置
log_level = "info"

# 默认策略
default_lease_ttl = "768h"
max_lease_ttl = "8760h"