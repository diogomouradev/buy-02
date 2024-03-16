FROM --platform=linux/amd64 maven:3.9.4-eclipse-temurin-17 as build

WORKDIR /app

COPY ./order-service/pom.xml ./order-service/
COPY ./order-service/src ./order-service/src/
COPY pom.xml .

WORKDIR /app/order-service

RUN mvn clean package -DskipTests

FROM bellsoft/liberica-openjdk-alpine:latest 

RUN apk add --no-cache bash
RUN apk add --no-cache curl

WORKDIR /app

COPY --from=build /app/order-service/target/order-service-*.jar order-service.jar

COPY wait-for-it.sh /wait-for-it.sh
COPY healthcheck.sh /healthcheck.sh
RUN chmod +x /wait-for-it.sh
RUN chmod +x /healthcheck.sh

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 CMD [ "/healthcheck.sh", "order" ]

CMD ["/wait-for-it.sh", "kafka:9092", "--", "java", "-jar", "order-service.jar"]
