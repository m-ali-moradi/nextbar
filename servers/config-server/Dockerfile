FROM maven:3.9.8-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml /workspace/parent-pom.xml
RUN mvn -q -N install -f /workspace/parent-pom.xml

COPY servers/config-server/pom.xml ./servers/config-server/
RUN mvn -q -DskipTests dependency:go-offline -f ./servers/config-server/pom.xml

COPY servers/config-server/src ./servers/config-server/src
RUN mvn -q -DskipTests clean package -f ./servers/config-server/pom.xml

RUN java -Djarmode=layertools -jar servers/config-server/target/*.jar extract --destination /extracted

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /extracted/dependencies/ ./
COPY --from=builder /extracted/spring-boot-loader/ ./
COPY --from=builder /extracted/snapshot-dependencies/ ./
COPY --from=builder /extracted/application/ ./

USER spring:spring

EXPOSE 8888

ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=40s \
    CMD wget -qO- http://localhost:8888/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]