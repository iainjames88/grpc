package uk.me.maitland.grpc.chat;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Jwts;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import uk.me.maitland.grpc.auth.AuthGrpc;
import uk.me.maitland.grpc.auth.AuthOuterClass.AuthenticationRequest;
import uk.me.maitland.grpc.auth.AuthOuterClass.AuthenticationResponse;

@Slf4j
public class AuthService extends AuthGrpc.AuthImplBase {
  private static final Map<String, String> USERS = new HashMap<>();

  @Override
  public void authenticate(
      AuthenticationRequest request, StreamObserver<AuthenticationResponse> responseObserver) {
    if (userIsRegistered(request)) {
      loginUser(request, responseObserver);
    } else {
      registerUser(request, responseObserver);
    }
  }

  private boolean userIsRegistered(AuthenticationRequest request) {
    return USERS.containsKey(request.getUsername());
  }

  private void loginUser(
      AuthenticationRequest request, StreamObserver<AuthenticationResponse> responseObserver) {
    if (!USERS.get(request.getUsername()).equals(request.getPassword())) {
      log.warn("{} failed to authenticate", request.getUsername());
      responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
    } else {
      log.info("{} authenticated", request.getUsername());
      String jwt = getJwt(request.getUsername());
      responseObserver.onNext(AuthenticationResponse.newBuilder().setJwt(jwt).build());
      responseObserver.onCompleted();
    }
  }

  private void registerUser(
      AuthenticationRequest request, StreamObserver<AuthenticationResponse> responseObserver) {
    log.info("{} registered", request.getUsername());
    String jwt = getJwt(request.getUsername());
    USERS.put(request.getUsername(), request.getPassword());
    responseObserver.onNext(AuthenticationResponse.newBuilder().setJwt(jwt).build());
    responseObserver.onCompleted();
  }

  private String getJwt(String username) {
    return Jwts.builder().setSubject(username).signWith(Constants.SIGNING_KEY).compact();
  }
}
