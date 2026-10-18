# ---------- 第一阶段：用带 Maven 的镜像编译打包 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 海外服务器直连 Maven Central，不配国内镜像
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 再复制源码并打包
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- 第二阶段：只带 JRE 的运行镜像 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]