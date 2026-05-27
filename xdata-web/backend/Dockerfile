# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the local repository and pom.xml first to cache dependencies
COPY libs-repo ./libs-repo
COPY pom.xml .

# Build dependencies
RUN mvn dependency:go-offline -B

# Copy source and build the application
COPY src ./src
RUN mvn package -DskipTests -B

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install runtime dependencies and Z3
RUN apt-get update && apt-get install -y \
    libgomp1 \
    z3 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Environment variables (can be overridden in docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
