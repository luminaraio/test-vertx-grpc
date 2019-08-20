FROM adoptopenjdk/openjdk11:alpine-jre

MAINTAINER Luminara Team <tech@luminara.io>

# Set the location of the verticles
ENV VERTX_HOME /usr/local/vertx

EXPOSE 8080

# Launch the verticle
WORKDIR $VERTX_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar vertx-app.jar -cluster"]

# Install grpc-health-probe
RUN GRPC_HEALTH_PROBE_VERSION=v0.3.0 && \
    wget -qO/bin/grpc_health_probe https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/${GRPC_HEALTH_PROBE_VERSION}/grpc_health_probe-linux-amd64 && \
    chmod +x /bin/grpc_health_probe

# Copy your fat jar and the config file to the to the container
COPY service/target/service-*.jar $VERTX_HOME/vertx-app.jar
