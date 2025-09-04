# ---------- build ----------
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
# Кешим зависимости
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests package

# ---------- runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]

# --- runtime ---
FROM eclipse-temurin:21-jre

# SSH-клиент + sshpass для парольной аутентификации
RUN apt-get update && apt-get install -y --no-install-recommends \
    openssh-client sshpass \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]