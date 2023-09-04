FROM amazoncorretto:17 as build
WORKDIR /app
COPY target .
EXPOSE 8765
CMD ["java", "-jar", "/app/rednet-api-gateway.jar"]