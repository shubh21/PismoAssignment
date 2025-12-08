 #---- Build stage ----
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only pom first to leverage dependency cache
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

# Now bring in source and build
COPY src ./src
RUN mvn -B -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy AS final
WORKDIR /

# Copy the built jar (adjust if your jar has a fixed name)
COPY --from=build /app/target/*.jar /app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]