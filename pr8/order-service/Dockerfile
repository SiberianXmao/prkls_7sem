FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./pom.xml
COPY order-service/pom.xml ./order-service/pom.xml
RUN mvn -B -q -f order-service/pom.xml dependency:go-offline
COPY order-service/src ./order-service/src
RUN mvn -B -q -f order-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /workspace/order-service/target/order-service-*.jar app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
