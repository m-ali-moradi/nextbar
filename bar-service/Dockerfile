FROM maven:3.9.8-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copy parent POM and install it for dependency resolution
COPY pom.xml /workspace/parent-pom.xml
RUN mvn -q -N install -f /workspace/parent-pom.xml

# Copy service POM and resolve dependencies
COPY bar-service/pom.xml ./bar-service/
RUN mvn -q -DskipTests dependency:go-offline -f ./bar-service/pom.xml

# Copy source and build
COPY bar-service/src ./bar-service/src
RUN mvn -q -DskipTests clean package -f ./bar-service/pom.xml

# Extract layered JAR
RUN java -Djarmode=layertools -jar bar-service/target/*.jar extract --destination /extracted

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

# Copy layers separately for optimal Docker caching
COPY --from=builder /extracted/dependencies/ ./
COPY --from=builder /extracted/spring-boot-loader/ ./
COPY --from=builder /extracted/snapshot-dependencies/ ./
COPY --from=builder /extracted/application/ ./

USER spring:spring

EXPOSE 8081

ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=40s \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]