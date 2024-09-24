# Use a base image with OpenJDK 17
FROM openjdk:22-jdk-slim
# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY /target/slackForwarder-0.0.1-SNAPSHOT.jar /app/app.jar

COPY src/main/resources/keystore.p12 /app/keystore.p12

# Set environment variables (optional)
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the port your app runs on (8080)
EXPOSE 8443

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

