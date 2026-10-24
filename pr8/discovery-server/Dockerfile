FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./pom.xml
COPY discovery-server/pom.xml ./discovery-server/pom.xml
RUN mvn -B -q -f discovery-server/pom.xml dependency:go-offline
COPY discovery-server/src ./discovery-server/src
RUN mvn -B -q -f discovery-server/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /workspace/discovery-server/target/discovery-server-*.jar app.jar
USER spring:spring
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
