# ---------- 第一阶段：用带 Maven 的镜像编译打包 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 配置阿里云 Maven 镜像，国内下载依赖快很多
RUN mkdir -p /root/.m2 \
    && printf '%s' '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# 先复制 pom 并预下载依赖。这一步单独成层，
# 只要 pom 没改，后续重建就能命中缓存，不用重新下载。
COPY pom.xml .
RUN mvn -q dependency:go-offline

# 再复制源码并打包
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---------- 第二阶段：只带 JRE 的运行镜像 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]