package uk.me.maitland.grpc.chat;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthServerInterceptor implements ServerInterceptor {
  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    String jwt = headers.get(Constants.JWT);
    if (jwt == null) {
      log.warn("JWT missing from request metadata");
      call.close(Status.UNAUTHENTICATED.withDescription("JWT missing"), headers);
      return new ServerCall.Listener<>() {};
    }

    try {
      Jws<Claims> decodedJwt =
          Jwts.parserBuilder().setSigningKey(Constants.SIGNING_KEY).build().parseClaimsJws(jwt);
      return Contexts.interceptCall(
          Context.current().withValue(Constants.DECODED_JWT, decodedJwt), call, headers, next);
    } catch (JwtException e) {
      log.error("Could not parse JWT", e);
      call.close(Status.UNAUTHENTICATED.withDescription("Invalid JWT"), headers);
      return new ServerCall.Listener<>() {};
    }
  }
}
