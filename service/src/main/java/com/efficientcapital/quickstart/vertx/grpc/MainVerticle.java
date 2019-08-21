package com.efficientcapital.quickstart.vertx.grpc;

import com.efficientcapital.commons.vertx.verticle.AbstractMainVerticle;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.services.HealthStatusManager;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;

/**
 * Created by Luminara Team.
 */
public class MainVerticle extends AbstractMainVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  protected void deployVerticles(JsonObject config) {
    // Deploy gRPC Verticle
    HealthStatusManager healthStatusManager = new HealthStatusManager();
    vertx.deployVerticle(new GrpcVerticle(discovery,
        List.of(new HelloService(),
          ProtoReflectionService.newInstance(),
          healthStatusManager.getHealthService())),
      new DeploymentOptions().setConfig(config));
  }
}
