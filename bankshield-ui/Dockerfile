# bankshield-ui/Dockerfile
FROM node:16-alpine as build

# 设置工作目录
WORKDIR /app

# 复制package文件
COPY package*.json ./

# 安装依赖
RUN npm ci --only=production

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 生产环境镜像
FROM nginx:alpine

# 安装curl用于健康检查
RUN apk add --no-cache curl

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# 复制构建产物
COPY --from=build /app/dist /usr/share/nginx/html

# 创建非root用户
RUN addgroup -g 1001 -S bankshield && \
    adduser -S bankshield -u 1001 -G bankshield

# 设置文件权限
RUN chown -R bankshield:bankshield /usr/share/nginx/html && \
    chown -R bankshield:bankshield /var/cache/nginx && \
    chown -R bankshield:bankshield /var/log/nginx && \
    chown -R bankshield:bankshield /etc/nginx/conf.d

# 切换到非root用户
USER bankshield

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:80/ || exit 1

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]