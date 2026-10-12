# Build with: docker build --build-arg MODULE=services/auth-service -t shopsphere/auth-service .
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY common-lib/pom.xml common-lib/pom.xml
COPY services services
COPY common-lib/src common-lib/src
ARG MODULE
RUN mvn -B -ntp -pl ${MODULE} -am -DskipTests package
RUN java -Djarmode=layertools -jar ${MODULE}/target/*.jar extract --destination /layers

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S shopsphere && adduser -S shopsphere -G shopsphere
WORKDIR /app
COPY --from=build /layers/dependencies/ ./
COPY --from=build /layers/spring-boot-loader/ ./
COPY --from=build /layers/snapshot-dependencies/ ./
COPY --from=build /layers/application/ ./
USER shopsphere
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-XX:+UseSerialGC","org.springframework.boot.loader.launch.JarLauncher"]
