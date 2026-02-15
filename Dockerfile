# Multi-stage build para Consumer Schedules
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa de producción
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Crear directorio para almacenar archivos JSON
RUN mkdir -p /app/update_rutas

# Exponer puerto
EXPOSE 8084

# Usuario no root
RUN addgroup -S spring && adduser -S spring -G spring
RUN chown -R spring:spring /app/update_rutas
USER spring:spring

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
