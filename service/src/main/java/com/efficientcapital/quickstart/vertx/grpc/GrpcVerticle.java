package com.efficientcapital.quickstart.vertx.grpc;

import io.grpc.BindableService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Luminara Team.
 */
public class GrpcVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrpcVerticle.class);
  private final List<BindableService> services;
  private final ServiceDiscovery discovery;
  private VertxServer vertxServer;

  public GrpcVerticle(ServiceDiscovery discovery, List<BindableService> services) {
    this.discovery = discovery;
    this.services = services;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertxServer = startGrpcServer(vertx, startPromise,
      config().getJsonObject("grpc").getString("host", "0.0.0.0"),
      config().getJsonObject("grpc").getInteger("port"),
      services);
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    vertxServer.shutdown(stopPromise);
    super.stop(stopPromise);
  }

  private VertxServer startGrpcServer(Vertx vertx, Promise<Void> startPromise, String host, Integer port,
                                      List<BindableService> grpcServices) {
    // Configure host and port
    VertxServerBuilder vertxServerBuilder = VertxServerBuilder.forAddress(vertx, host, port);

    // Add all the bindable services
    grpcServices.forEach(vertxServerBuilder::addService);

    // Create Vert.x gRPC server
    VertxServer vertxServer = vertxServerBuilder
      .build();

    vertxServer.start(completionHandler -> {
      if (completionHandler.succeeded()) {
        LOGGER.debug("Server running: \nHost: {} \nPort: {}", host, port);
//        startPromise.complete();
        if (config().containsKey("KUBERNETES_NAMESPACE")) {
          startPromise.complete();
        } else {
          Record record = new Record()
            .setName(config().getString("serviceName"))
            .setLocation(
              new JsonObject()
                .put("host", host)
                .put("port", port));
          LOGGER.info("Adding a gRPC service record into the service discovery backend. " +
            "\nRecord details: " +
            "\nname: {} " +
            "\nlocation: {}", record.getName(), record.getLocation());
          discovery.publish(record, result -> {
            if (result.succeeded()) {
              LOGGER.debug("gRPC service ({}) record has been published to the service registry",
                config().getString("serviceName"));
              startPromise.complete();
              return;
            }
            LOGGER.error("Can't register the record with the service registry.", result.cause());
            startPromise.fail(result.cause());
          });
        }
      } else {
        startPromise.fail(completionHandler.cause());
      }
    });

    return vertxServer;
  }
}
