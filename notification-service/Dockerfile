FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

COPY ../gradlew .
COPY ../gradle gradle
COPY ../build.gradle .
COPY ../settings.gradle .
COPY ../common ./common
COPY ../notification-service ./notification-service

RUN chmod +x gradlew
RUN ./gradlew :notification-service:bootJar -x test

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
ARG JAR_FILE=/workspace/app/notification-service/build/libs/*.jar
COPY --from=build ${JAR_FILE} app.jar

# Add health check
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q -O /dev/null http://localhost:8081/actuator/health || exit 1

# Use JVM options specific to containers - better memory handling and faster startup
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app.jar"]