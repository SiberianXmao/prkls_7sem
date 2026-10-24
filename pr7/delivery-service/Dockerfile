FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY order-service/pom.xml order-service/pom.xml
COPY delivery-service/pom.xml delivery-service/pom.xml
RUN mvn -B -q -pl delivery-service -am dependency:go-offline

COPY delivery-service/src delivery-service/src
RUN mvn -B -q -pl delivery-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /workspace/delivery-service/target/delivery-service-*.jar app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
