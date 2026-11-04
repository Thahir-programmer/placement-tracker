# Build stage: has Maven and the full JDK, and is thrown away after the build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copying pom.xml first lets Docker reuse the downloaded dependencies when only src/ changes
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# Run stage: only the JRE and the finished jar end up in the final image
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Don't run the app as root
RUN useradd --system --no-create-home appuser
USER appuser

# Documents the default; the app listens on $PORT when the host sets it
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
