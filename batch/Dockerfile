FROM openjdk:17-jdk-slim AS builder
WORKDIR /build

COPY gradle gradle/
COPY batch/build.gradle batch/
COPY common/build.gradle common/
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
RUN chmod +x ./gradlew

COPY batch batch/
COPY common common/
RUN ./gradlew clean :batch:bootJar --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /build/batch/build/libs/*.jar batch.jar

ENTRYPOINT ["java", "-jar", "batch.jar"]
