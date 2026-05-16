# Stage 1: Builder - zbuduj aplikację
FROM maven:3.9.8-eclipse-temurin-21 AS builder

WORKDIR /app

# Skopiuj pliki pom.xml
COPY pom.xml .

# Pobierz dependencje
RUN mvn dependency:go-offline

# Skopiuj kod źródłowy
COPY src/ src/

# Zbuduj aplikację
RUN mvn clean package -DskipTests

# Stage 2: Runtime - uruchom aplikację
FROM eclipse-temurin:21-jre-noble

WORKDIR /app

# Skopiuj skompilowany JAR z poprzedniego stage'a
COPY --from=builder /app/target/pietrzak-ogryzek-project-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Uruchom aplikację
ENTRYPOINT ["java", "-jar", "app.jar"]

