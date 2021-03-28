package uk.me.maitland.grpc.chat;

import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import uk.me.maitland.grpc.auth.AuthGrpc;
import uk.me.maitland.grpc.auth.AuthOuterClass.AuthenticationRequest;
import uk.me.maitland.grpc.auth.AuthOuterClass.AuthenticationResponse;

@Slf4j
public class AuthGrpcClient {

  private final AuthGrpc.AuthBlockingStub blockingStub;

  public AuthGrpcClient(Channel channel) {
    blockingStub = AuthGrpc.newBlockingStub(channel);
  }

  public String authenticate(String username, String password) {
    try {
      AuthenticationResponse response =
          blockingStub.authenticate(
              AuthenticationRequest.newBuilder()
                  .setUsername(username)
                  .setPassword(password)
                  .build());
      return response.getJwt();
    } catch (StatusRuntimeException e) {
      log.error("Error calling Auth/Authenticate", e);
      throw e;
    }
  }
}
