# ─── STAGE 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
 
WORKDIR /app
 
COPY pom.xml .
COPY src ./src
 
RUN mvn clean package -DskipTests -B
 
# ─── STAGE 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
 
# Cria grupo e usuário não privilegiado automaticamente no build
RUN groupadd -r appgroup && \
    useradd -r -g appgroup -s /sbin/nologin appuser
 
WORKDIR /app
 
# Copia o jar já com ownership correto (uma única layer)
COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar
 
# Variável de ambiente obrigatória pelo edital
ENV APP_PORT=8080
 
# Troca para usuário não privilegiado
USER appuser
 
EXPOSE 8080
 
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${APP_PORT}"]

