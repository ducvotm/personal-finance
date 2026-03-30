# Maven build container
FROM eclipse-temurin:17-jdk AS maven_build
WORKDIR /tmp/
RUN apt-get update && apt-get install -y maven
COPY pom.xml /tmp/
COPY eclipse-java-formatter.xml /tmp/
COPY src /tmp/src/
RUN mvn clean package -DskipTests -q

#pull base image
FROM eclipse-temurin:17-jre
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx2048m"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar" ]
COPY --from=maven_build /tmp/target/*.jar /app.jar