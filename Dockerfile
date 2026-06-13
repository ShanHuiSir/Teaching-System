FROM docker.1ms.run/library/eclipse-temurin:17-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=fake \
    APP_UPLOAD_ROOT=/data/uploads \
    JAVA_OPTS=""

RUN mkdir -p /data/uploads
COPY target/*.jar /app/app.jar

EXPOSE 8080
VOLUME ["/data/uploads"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
