package uk.me.maitland.grpc.chat;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import uk.me.maitland.grpc.todo.ToDoServer;

@Slf4j
public class ChatServer {
  public static void main(String[] args) {
    int port = 5001;
    InputStream fullChain = ChatServer.class.getClassLoader().getResourceAsStream("fullchain.pem");
    InputStream privateKey = ToDoServer.class.getClassLoader().getResourceAsStream("privkey.pem");

    Server server =
        ServerBuilder.forPort(port)
            .useTransportSecurity(fullChain, privateKey)
            .addService(new AuthService())
            .build();

    try {
      log.info("Starting server on port {}", port);
      server.start();
      server.awaitTermination();
    } catch (IOException | InterruptedException e) {
      log.error("Something went wrong", e);
    }
  }
}
