FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Nota: o build completo é feito via docker-compose com context do módulo
# Para build standalone: mvn package -DskipTests no diretório raiz do projeto
COPY target/*.jar app.jar
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=build /app/app.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:default}", "app.jar"]
