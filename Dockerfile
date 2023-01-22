FROM openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file and other necessary files into the container
COPY build/libs/demo-0.0.1-SNAPSHOT.jar /app/app.jar
COPY src/main/resources/application.properties /app/config/

# Expose the port your app runs on
EXPOSE 8080

# Run the jar file
CMD ["java", "-jar", "app.jar"]