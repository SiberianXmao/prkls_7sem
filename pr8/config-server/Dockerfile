FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./pom.xml
COPY config-server/pom.xml ./config-server/pom.xml
RUN mvn -B -q -f config-server/pom.xml dependency:go-offline
COPY config-server/src ./config-server/src
RUN mvn -B -q -f config-server/pom.xml package -DskipTests

FROM alpine/git:2.49.1 AS repository
WORKDIR /config-repo
COPY config-repo/ ./
RUN git init -b main \
    && git config user.name "PR8 Config" \
    && git config user.email "config@pr8.local" \
    && git add . \
    && git commit -m "Initial configuration"

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /workspace/config-server/target/config-server-*.jar app.jar
COPY --from=repository --chown=spring:spring /config-repo /config-repo
RUN mkdir -p /home/spring/.config && chown -R spring:spring /home/spring
USER spring:spring
ENV HOME=/home/spring XDG_CONFIG_HOME=/home/spring/.config
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
