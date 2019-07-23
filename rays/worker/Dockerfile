#
FROM openjdk:13-alpine
ARG JAR_FILE
ARG DATA_DIR
#
COPY ${JAR_FILE} app.jar
COPY ${DATA_DIR} ./data/
#
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
