# --- Stage 1: Build the Java application ---
# Use an official Maven image with a specific JDK version
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build a single executable JAR with all dependencies
RUN mvn clean package shade:shade

# --- Stage 2: Create the final, lightweight runtime image ---
# Use a base OpenJDK image that's just enough to run the JAR
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
# Copy the executable JAR from the build stage
COPY --from=build /app/target/duckdb_java-1.0-SNAPSHOT.jar app.jar
# Expose the FlightSQL server port
EXPOSE 31337
# Set the entrypoint to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
