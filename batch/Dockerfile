FROM openjdk:17-jdk-slim AS builder
WORKDIR /build

COPY gradle gradle/
COPY api/build.gradle api/
COPY batch/build.gradle batch/
COPY common/build.gradle common/
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
RUN chmod +x ./gradlew

RUN ./gradlew :batch:dependencies --no-daemon

COPY api api/
COPY batch batch/
COPY common common/
RUN ./gradlew :batch:build -x test --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=builder /build/batch/build/libs/*.jar batch.jar

ENTRYPOINT java -jar batch.jar