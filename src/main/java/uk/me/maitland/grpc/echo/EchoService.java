package uk.me.maitland.grpc.echo;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.me.maitland.grpc.echo.EchoServiceGrpc.EchoServiceImplBase;

public class EchoService extends EchoServiceImplBase {
  private static final Logger LOG = LoggerFactory.getLogger(EchoService.class);

  @Override
  public void echo(EchoRequest request, StreamObserver<EchoResponse> responseObserver) {
    LOG.info("Handling call to EchoService::echo");

    responseObserver.onNext(EchoResponse.newBuilder().setMessage(request.getMessage()).build());
    responseObserver.onCompleted();
  }
}
