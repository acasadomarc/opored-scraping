FROM eclipse-temurin:21-jdk-alpine
LABEL authors="antonio.casado"

WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]