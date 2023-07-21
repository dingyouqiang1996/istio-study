FROM java:8
VOLUME /tmp
ADD comsumer-1.0-SNAPSHOT.jar /comsumer-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/comsumer-1.0-SNAPSHOT.jar"]