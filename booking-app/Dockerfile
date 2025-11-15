FROM gradle:8.10-jdk17 AS deps
WORKDIR /app

COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./

COPY booking-app/build.gradle ./booking-app/build.gradle

RUN ./gradlew :booking-app:dependencies --no-daemon --refresh-dependencies

FROM gradle:8.10-jdk17 AS builder
WORKDIR /app

COPY --from=deps /home/gradle/.gradle /home/gradle/.gradle
COPY --from=deps /app/gradle /app/gradle
COPY --from=deps /app/gradlew /app/gradlew
COPY --from=deps /app/settings.gradle /app/settings.gradle
COPY --from=deps /app/build.gradle /app/build.gradle
COPY --from=deps /app/booking-app/build.gradle /app/booking-app/build.gradle

COPY . .

RUN ./gradlew :booking-app:bootJar --no-daemon -x test --build-cache

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=builder --chown=spring:spring /app/booking-app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]