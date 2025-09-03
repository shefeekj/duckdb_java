# --- Stage 1: Build the Java application ---
# Use an official Maven image to compile and package the application
FROM maven:3.8.7-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build a single executable JAR with all dependencies
RUN mvn clean package shade:shade

# --- Stage 2: Create the final, lightweight runtime image ---
# Use a base OpenJDK image that's just enough to run the JAR
FROM openjdk:17-jre-slim
WORKDIR /app
# Copy the executable JAR from the build stage
COPY --from=build /app/target/duckdb_java-1.0-SNAPSHOT.jar app.jar
# Expose the FlightSQL server port
EXPOSE 31337
# Set the entrypoint to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
```
eof

### Next Steps to Build and Run

1.  **Add `Dockerfile` to Your Repository:** Copy the code above into a new file named `Dockerfile` in the root of your `duckdb_java` repository.

2.  **Build the Docker Image:** Run this command from the root of your repository. This will use Maven to compile your code and then create the final Docker image. 
    ```bash
    docker build -t duckdb_java_image .
    ```

3.  **Run the Container:** Run the image, mapping the port and providing your S3 pre-signed URLs as environment variables. The Java code will read these URLs to perform the join query.
    ```bash
    docker run -d \
      -p 31337:31337 \
      -e S3_FILE1_URL="<your-presigned-url-1>" \
      -e S3_FILE2_URL="<your-presigned-url-2>" \
      --name duckdb_java_container \
      duckdb_java_image
    
