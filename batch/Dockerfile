FROM eclipse-temurin:17-jre
WORKDIR /app
COPY batch/build/libs/*.jar app.jar
COPY batch/src/main/resources/source /app/source
ENTRYPOINT ["java", "-Dresource.path=file:/app/source/", "-jar", "app.jar"]