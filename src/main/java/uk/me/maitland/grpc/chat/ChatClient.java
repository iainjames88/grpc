package uk.me.maitland.grpc.chat;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatClient {
  public static void main(String[] args) throws IOException {
    Channel authChannel = ManagedChannelBuilder.forAddress("maitland.me.uk", 5001).build();
    AuthGrpcClient authGrpcClient = new AuthGrpcClient(authChannel);

    ChatClient chatClient = new ChatClient();
    String jwt = chatClient.authenticate(authGrpcClient);
    log.info("authenticated with jwt {}", jwt);
  }

  public String authenticate(AuthGrpcClient authGrpcClient) {
    try {
      Map<String, String> credentials = getCredentials();
      return authGrpcClient.authenticate(credentials.get("username"), credentials.get("password"));
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode().equals(Code.UNAUTHENTICATED)) {
        System.out.println("Failed to authenticate - try again");
        return authenticate(authGrpcClient);
      }

      throw e;
    }
  }

  public Map<String, String> getCredentials() {
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("username:");
      String username = bufferedReader.readLine();

      System.out.println("password:");
      String password = bufferedReader.readLine();

      return Map.of("username", username, "password", password);
    } catch (IOException e) {
      log.error("Something went wrong initialising the ChatClient", e);
      return getCredentials();
    }
  }
}
