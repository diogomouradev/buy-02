FROM maven:3.9.4-eclipse-temurin-17 as build

WORKDIR /app

COPY ./product-service/pom.xml ./product-service/
COPY ./product-service/src ./product-service/src/
COPY pom.xml .

WORKDIR /app/product-service

RUN mvn clean package -DskipTests

FROM bellsoft/liberica-openjdk-alpine:latest 

RUN apk add --no-cache bash
RUN apk add --no-cache curl

WORKDIR /app

COPY --from=build /app/product-service/target/product-service-*.jar product-service.jar

COPY wait-for-it.sh /wait-for-it.sh
COPY healthcheck.sh /healthcheck.sh
RUN chmod +x /wait-for-it.sh
RUN chmod +x /healthcheck.sh

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 CMD [ "/healthcheck.sh", "product" ]

CMD ["/wait-for-it.sh", "kafka:9092", "--", "java", "-jar", "product-service.jar"]
