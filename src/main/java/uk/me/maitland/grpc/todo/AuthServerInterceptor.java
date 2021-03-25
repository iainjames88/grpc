package uk.me.maitland.grpc.todo;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthServerInterceptor implements ServerInterceptor {
  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    if (headers.get(Constants.JWT) == null) {
      log.error("JWT missing from request metadata");
      call.close(Status.UNAUTHENTICATED.withDescription("JWT missing"), headers);
      return new ServerCall.Listener<>() {};
    }

    return Contexts.interceptCall(
        Context.current().withValue(Constants.DECODED_JWT, "decoded_jwt"), call, headers, next);
  }
}
