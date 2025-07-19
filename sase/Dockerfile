# Stage 1: build del progetto con Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia tutto il progetto
COPY . .

# Compila e crea il jar (senza test)
RUN mvn clean package -DskipTests

# Stage 2: immagine runtime leggera
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copia il jar generato dal build
COPY --from=build /app/target/*.jar app.jar

# Espone la porta Spring (modifica se usi un'altra)
EXPOSE 8080

# Comando di avvio
CMD ["java", "-jar", "app.jar"]
