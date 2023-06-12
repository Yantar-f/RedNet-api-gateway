FROM amazoncorretto:17 as build
WORKDIR /app
COPY target .
EXPOSE 9000
CMD ["java", "-jar", "/app/red-net-api-gateway.jar"]