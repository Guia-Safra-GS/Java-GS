# dockerfile base para deploy
# dockerfile base para deploy

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar


ENV APP_PORT=8080

# Transfere ownership para o usuário criado
RUN chown appuser:appgroup app.jar

# Roda como usuário não privilegiado
USER appuser

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${APP_PORT}"]
