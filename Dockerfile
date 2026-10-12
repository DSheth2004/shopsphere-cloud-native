# syntax=docker/dockerfile:1.7
# One multi-stage Dockerfile for every Spring Boot service:
#   docker build --build-arg MODULE=services/auth-service -t shopsphere/auth-service .
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
# Copy the POMs first so dependency resolution is cached until a POM changes.
COPY pom.xml .
COPY common-lib/pom.xml common-lib/pom.xml
COPY services/api-gateway/pom.xml services/api-gateway/pom.xml
COPY services/auth-service/pom.xml services/auth-service/pom.xml
COPY services/product-service/pom.xml services/product-service/pom.xml
COPY services/inventory-service/pom.xml services/inventory-service/pom.xml
COPY services/cart-service/pom.xml services/cart-service/pom.xml
COPY services/order-service/pom.xml services/order-service/pom.xml
COPY services/payment-service/pom.xml services/payment-service/pom.xml
COPY services/notification-service/pom.xml services/notification-service/pom.xml
COPY services/report-job/pom.xml services/report-job/pom.xml
COPY common-lib/src common-lib/src
COPY services services
ARG MODULE
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -q -pl ${MODULE} -am -DskipTests package \
 && cp ${MODULE}/target/*.jar /workspace/app.jar \
 && java -Djarmode=tools -jar /workspace/app.jar extract --layers --launcher --destination /layers

FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.source="https://github.com/DSheth2004/shopsphere-cloud-native" \
      org.opencontainers.image.description="ShopSphere Spring Boot microservice"
RUN apk upgrade --no-cache && addgroup -S -g 10001 shopsphere && adduser -S -u 10001 -G shopsphere shopsphere
WORKDIR /app
COPY --from=build /layers/dependencies/ ./
COPY --from=build /layers/spring-boot-loader/ ./
COPY --from=build /layers/snapshot-dependencies/ ./
COPY --from=build /layers/application/ ./
# Every service listens on 8080 inside its container; small-heap JVM flags keep the whole stack on one node.
ENV SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -XX:ReservedCodeCacheSize=48m -Djava.security.egd=file:/dev/./urandom"
USER 10001
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 CMD wget -qO- http://127.0.0.1:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
