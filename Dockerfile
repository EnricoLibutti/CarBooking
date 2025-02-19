FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/carbooking.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]