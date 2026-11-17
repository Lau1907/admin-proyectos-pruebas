# Etapa 1: Compilacion (Java 21 + Maven 3.9.9)
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# Etapa 2: Imagen de ejecucion ligera (JRE 21 Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV SERVER_PORT=8080
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-${SERVER_PORT:-8080}} -jar app.jar"]
