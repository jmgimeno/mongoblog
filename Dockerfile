FROM anapsix/alpine-java

COPY target/MongoBlog-1.0-SNAPSHOT-jar-with-dependencies.jar /app/

CMD java -jar /app/MongoBlog-1.0-SNAPSHOT-jar-with-dependencies.jar