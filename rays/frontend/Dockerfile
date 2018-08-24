#
FROM openjdk:10-jre
ARG JAR_FILE
VOLUME /tmp
EXPOSE 8080
#
COPY ${JAR_FILE} app.jar
#
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]