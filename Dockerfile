FROM gradle:8.7-jdk17 AS deps
WORKDIR /app

COPY gradle gradle
COPY gradlew settings.gradle ./
COPY build.gradle ./

RUN ./gradlew dependencies --no-daemon --refresh-dependencies

FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY --from=deps /home/gradle/.gradle /home/gradle/.gradle
COPY --from=deps /app/gradle /app/gradle
COPY --from=deps /app/gradlew /app/gradlew
COPY --from=deps /app/settings.gradle /app/settings.gradle
COPY --from=deps /app/build.gradle /app/build.gradle

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test --build-cache

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]