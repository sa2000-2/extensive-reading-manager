FROM amazoncorretto:25 AS build
WORKDIR /home/app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN yum install -y tar gzip

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM amazoncorretto:25-alpine
WORKDIR /home/app
COPY --from=build /home/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]