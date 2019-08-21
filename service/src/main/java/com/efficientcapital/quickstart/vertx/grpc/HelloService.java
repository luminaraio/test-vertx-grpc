package com.efficientcapital.quickstart.vertx.grpc;

import com.efficientcapital.quickstart.vertx.HelloReply;
import com.efficientcapital.quickstart.vertx.HelloRequest;
import com.efficientcapital.quickstart.vertx.GreeterGrpc;
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
