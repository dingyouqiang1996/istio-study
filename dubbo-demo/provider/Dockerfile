FROM java:8
VOLUME /tmp
ADD provider-1.0-SNAPSHOT.jar /provider-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/provider-1.0-SNAPSHOT.jar"]