FROM java:8

ENV PERSEO_HOST=localhost 
ENV PERSEO_PORT=9090 
ENV ORION_HOST=localhost
ENV ORION_PORT=1026
ENV FIWARE_SERVICE=howtoservice
ENV FIWARE_SERVICEPATH=/howto
ENV JDBC_URL_STRING=jdbc:mysql://127.0.0.1:3306/perseo
ENV JDBC_USER=root
ENV JDBC_PASSWORD=bosonit2018
ENV EUREKA_VHOST=host
ENV EUREKA_PORT=1111
ENV SECRET=_JWT_TOKEN_KEY_

RUN apt-get update
RUN apt-get install -y maven

WORKDIR /
EXPOSE 5000
ADD . /opt/
WORKDIR /opt/
RUN ["mvn", "clean"]
RUN mvn package -X -Dmaven.test.skip=true
RUN mv target/fiwoo_rules-External_Actions-0.0.1-SNAPSHOT.jar target/fiwoo_rules.jar

CMD java -jar target/fiwoo_rules.jar

