FROM node:24-alpine AS frontend-build
WORKDIR /workspace/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /workspace
COPY backend/pom.xml backend/pom.xml
RUN mvn -B -f backend/pom.xml dependency:go-offline
COPY backend/src backend/src
COPY --from=frontend-build /workspace/frontend/dist frontend/dist
RUN mvn -B -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /workspace/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
