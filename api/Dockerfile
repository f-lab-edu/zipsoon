FROM openjdk:17-jdk-slim AS builder
WORKDIR /build

COPY gradle gradle/
COPY api/build.gradle api/
COPY common/build.gradle common/
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
RUN chmod +x ./gradlew

RUN ./gradlew clean :api:bootJar --no-daemon

COPY api api/
COPY common common/

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /build/api/build/libs/*.jar api.jar

ENTRYPOINT ["java", "-jar", "api.jar"]