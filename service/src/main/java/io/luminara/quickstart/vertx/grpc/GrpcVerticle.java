package io.luminara.quickstart.vertx.grpc;

import io.grpc.BindableService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Luminara Team.
 */
public class GrpcVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrpcVerticle.class);
  private final List<BindableService> services;
  private VertxServer vertxServer;

  public GrpcVerticle(List<BindableService> services) {
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
        startPromise.complete();
      } else {
        startPromise.fail(completionHandler.cause());
      }
    });

    return vertxServer;
  }
}
