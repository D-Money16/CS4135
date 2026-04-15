FROM maven:3.9-eclipse-temurin-21 AS build
ARG SERVICE_NAME
WORKDIR /workspace
COPY . .
RUN mvn package -pl ${SERVICE_NAME} -am -DskipTests -Dmaven.test.skip=true -q

FROM eclipse-temurin:21-jre-jammy
ARG SERVICE_NAME
COPY --from=build /workspace/${SERVICE_NAME}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
