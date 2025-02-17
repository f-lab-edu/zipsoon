FROM openjdk:17-jdk-slim AS builder
WORKDIR /build

COPY gradle gradle/
COPY api/build.gradle api/
COPY common/build.gradle common/
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
RUN chmod +x ./gradlew

COPY api api/
COPY common common/
RUN ./gradlew clean :api:bootJar --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /build/api/build/libs/*.jar api.jar

ENTRYPOINT ["java", "-jar", "api.jar"]
