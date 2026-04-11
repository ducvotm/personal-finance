FROM eclipse-temurin:17-jre AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:17-jre
WORKDIR /app
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx2048m"

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar" ]