
FROM openjdk:17-bullseye
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*
WORKDIR /home
COPY target/video-0.0.1-SNAPSHOT.jar video-0.0.1-SNAPSHOT.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "video-0.0.1-SNAPSHOT.jar"]