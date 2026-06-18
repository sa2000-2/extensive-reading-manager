# 1. ビルドステージ
FROM maven:3.9.9-amazoncorretto-25 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests

# 2. 実行ステージ
FROM amazoncorretto:25-alpine
COPY --from=build /home/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]