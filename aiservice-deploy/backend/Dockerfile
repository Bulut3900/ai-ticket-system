# 使用轻量级的 JDK 25 镜像
FROM eclipse-temurin:25-jre

WORKDIR /app

# 复制 JAR 文件
COPY ai-service-0.0.1-SNAPSHOT.jar app.jar

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]