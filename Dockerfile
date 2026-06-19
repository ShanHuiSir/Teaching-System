ARG REGISTRY=docker.io

FROM ${REGISTRY}/library/eclipse-temurin:17-jre
WORKDIR /app

# Default "fake" profile for standalone `docker run` usage — the AI evaluation
# will use mock responses so the container works without an AI service.
# When launched via docker-compose, SPRING_PROFILES_ACTIVE is overridden to
# "docker" which connects to the real ai-service container.
ENV SPRING_PROFILES_ACTIVE=fake \
    APP_UPLOAD_ROOT=/data/uploads \
    JAVA_OPTS=""

RUN mkdir -p /data/uploads

# Copy the fat jar (spring-boot-maven-plugin repackages into a single
# executable jar).  The .dockerignore excludes the *.original backup so
# the glob only matches the intended artifact.
COPY target/Teaching-System-*.jar /app/app.jar

EXPOSE 8080
VOLUME ["/data/uploads"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
