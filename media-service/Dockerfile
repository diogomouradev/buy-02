FROM --platform=linux/amd64 maven:3.9.4-eclipse-temurin-17 as build

WORKDIR /app

COPY ./media-service/pom.xml ./media-service/
COPY ./media-service/src ./media-service/src/
COPY pom.xml .

WORKDIR /app/media-service

RUN mvn clean package -DskipTests

FROM bellsoft/liberica-openjdk-alpine:latest 

RUN apk add --no-cache bash
RUN apk add --no-cache curl

WORKDIR /app

COPY --from=build /app/media-service/target/media-service-*.jar media-service.jar

COPY wait-for-it.sh /wait-for-it.sh
COPY healthcheck.sh /healthcheck.sh
RUN chmod +x /wait-for-it.sh
RUN chmod +x /healthcheck.sh

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 CMD [ "/healthcheck.sh", "media" ]

CMD ["/wait-for-it.sh", "kafka:9092", "--", "java", "-jar", "media-service.jar"]
