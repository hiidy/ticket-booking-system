FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon || return 0

COPY . .
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]