package io.luminara.quickstart.vertx.grpc;

import io.luminara.quickstart.vertx.GreeterGrpc;
import io.luminara.quickstart.vertx.HelloReply;
import io.luminara.quickstart.vertx.HelloRequest;
import io.vertx.core.Future;

/**
 * Created by Luminara Team.
 */
public class HelloService extends GreeterGrpc.GreeterVertxImplBase {

  @Override
  public void sayHello(HelloRequest request, Future<HelloReply> response) {
    response.complete(
      HelloReply.newBuilder()
        .setMessage(request.getName().isBlank() ? "Hello, World" : "Hello, " + request.getName())
        .build());
  }
}
