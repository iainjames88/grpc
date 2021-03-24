package uk.me.maitland.grpc.echo;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServer {
  private static final Logger LOG = LoggerFactory.getLogger(EchoServer.class);

  public static void main(String[] args) {
    int port = 5001;
    Server server = ServerBuilder.forPort(port).addService(new EchoService()).build();
    try {
      LOG.info("Starting server on port {}", port);

      server.start();
      server.awaitTermination();
    } catch (IOException | InterruptedException e) {
      LOG.error("Server encountered an error", e);
    }
  }
}
