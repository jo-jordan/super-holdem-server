# 使用带有JDK 17的官方Gradle镜像作为基础镜像
FROM gradle:8.1.1-jdk17 as builder

# 设置工作目录
WORKDIR /app

# 复制项目的构建文件
COPY build.gradle .
COPY settings.gradle .

# 复制源代码
COPY src src

# 运行构建命令
RUN gradle uberJar

# 开始第二个阶段，使用OpenJDK镜像
FROM openjdk:17-jdk

# 将构建的jar文件复制到新的容器中
COPY --from=builder /app/build/libs/*.jar /app.jar

# 暴露端口
EXPOSE 8888
EXPOSE 8887

# 运行应用
ENTRYPOINT ["java","-jar","/app.jar"]
