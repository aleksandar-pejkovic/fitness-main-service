FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/fitness-main-service-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
