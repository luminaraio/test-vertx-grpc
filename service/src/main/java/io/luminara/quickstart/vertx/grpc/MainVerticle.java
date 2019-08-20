package io.luminara.quickstart.vertx.grpc;

import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.services.HealthStatusManager;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;

/**
 * Created by Luminara Team.
 */
public class MainVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
  private ConfigRetriever retriever;

  @Override
  public void start(Promise<Void> startFuture) throws Exception {
    LOGGER.debug("MainVerticle.start(..)");

    configRetriever();
    retriever.getConfig(config -> {
      if (config.failed()) {
        startFuture.fail(config.cause());
      } else {
        // Deploy gRPC Verticle
        HealthStatusManager healthStatusManager = new HealthStatusManager();
        vertx.deployVerticle(new GrpcVerticle(
            List.of(new HelloService(),
              ProtoReflectionService.newInstance(),
              healthStatusManager.getHealthService())),
          new DeploymentOptions()
            .setConfig(config.result()));
      }
    });
  }

  private void configRetriever() {
    ConfigStoreOptions yamlStore = new ConfigStoreOptions()
      .setType("file")
      .setFormat("yaml")
      .setConfig(new JsonObject()
        .put("path", "application.yml"));

    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions()
        .addStore(yamlStore));
  }
}
