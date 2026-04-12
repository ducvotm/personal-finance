FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY eclipse-java-formatter.xml .
COPY src ./src

RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx2048m"

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/app.jar" ]