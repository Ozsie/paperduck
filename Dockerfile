# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/paperduck-0.0.1.jar ./paperduck.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/paperduck.jar"]
