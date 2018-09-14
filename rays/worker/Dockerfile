#
FROM openjdk:10-jre
ARG JAR_FILE
ARG DATA_DIR
#
COPY ${JAR_FILE} app.jar
COPY ${DATA_DIR} ./data/
#
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]