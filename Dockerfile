FROM ghcr.io/identicum/alpine-jdk25-maven:latest AS builder
WORKDIR /app
COPY . .
RUN mvn install -DskipTests


FROM ghcr.io/identicum/alpine-jre25:latest
WORKDIR /app
COPY --from=builder /app/target/*.jar ./app.jar
ENTRYPOINT ["java","-jar","./app.jar"]
