package com.efficientcapital.quickstart.vertx.grpc;

import com.efficientcapital.quickstart.vertx.HelloReply;
import com.efficientcapital.quickstart.vertx.HelloRequest;
import io.grpc.ManagedChannel;
import com.efficientcapital.quickstart.vertx.GreeterGrpc;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(VertxExtension.class)
@DisplayName("Grpc Verticle Test")
public class GrpcVerticleTest {
  private int port;

  @BeforeEach
  @DisplayName("Setup test by deploying verticles")
  void setup(Vertx vertx, VertxTestContext testContext) throws IOException {
    port = generatePortNumber();
    JsonObject config = buildBaseServerConfig(port);
    ServiceDiscovery discovery = ServiceDiscovery.create(
      vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config));

    vertx.deployVerticle(new GrpcVerticle(discovery, List.of(new GreeterGrpc.GreeterVertxImplBase() {
        @Override
        public void sayHello(HelloRequest request,
                             Future<HelloReply> response) {
          System.out.println("Hello " + request.getName());
          response.complete(
            HelloReply.newBuilder()
              .setMessage(request.getName())
              .build()
          );
        }
      })), buildDeploymentOptions(config),
      testContext.succeeding(id -> testContext.completeNow()));
  }

  private int generatePortNumber() throws IOException {
    ServerSocket socket = new ServerSocket(0);
    int localPort = socket.getLocalPort();
    socket.close();
    return localPort;
  }

  private JsonObject buildBaseServerConfig(int port) {
    return new JsonObject()
      .put("serviceName", "testService")
      .put("grpc", new JsonObject()
        .put("host", "localhost")
        .put("port", port));
  }


  private DeploymentOptions buildDeploymentOptions(JsonObject config) {
    return new DeploymentOptions()
      .setConfig(config);
  }

  @AfterEach
  @DisplayName("Check that the verticles is still there")
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs().isEmpty(), is(equalTo(false)));
    assertThat(vertx.deploymentIDs(), hasSize(1));
  }

  @DisplayName("Should echo back request")
  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void echoRequest(Vertx vertx, VertxTestContext testContext) {
    // GIVEN
    ManagedChannel grpcChannel = VertxChannelBuilder
      .forAddress(vertx, "localhost", port)
      .usePlaintext(true)
      .build();
    GreeterGrpc.GreeterVertxStub client = GreeterGrpc.newVertxStub(grpcChannel);

    // WHEN
    HelloRequest request =
      HelloRequest.newBuilder()
        .setName("John Doe")
        .build();
    client.sayHello(request,
      testContext.succeeding((HelloReply response) -> {
        testContext.verify(() -> {
          // THEN
          assertThat("Should echo back the name",
            response.getMessage(), is("John Doe"));
          grpcChannel.shutdownNow();
          testContext.completeNow();
        });
      }));
  }
}
