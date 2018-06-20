#
FROM mgorbunov/alpine-jre10
ARG JAR_FILE
VOLUME /spectra
VOLUME /tmp
#
COPY ${JAR_FILE} app.jar
#
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]