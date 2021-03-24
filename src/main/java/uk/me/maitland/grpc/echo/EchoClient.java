package uk.me.maitland.grpc.echo;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.me.maitland.grpc.echo.EchoServiceGrpc.EchoServiceBlockingStub;

public class EchoClient {
  private static final Logger LOG = LoggerFactory.getLogger(EchoClient.class);
  private final EchoServiceBlockingStub blockingStub;

  public EchoClient(Channel channel) {
    blockingStub = EchoServiceGrpc.newBlockingStub(channel);
  }

  public static void main(String[] args) throws InterruptedException {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 5001).usePlaintext().build();

    try {
      EchoClient echoClient = new EchoClient(channel);
      echoClient.echo("Hello, world");
    } finally {
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  public void echo(String message) {
    LOG.info("Calling EchoService::echo");

    EchoRequest request = EchoRequest.newBuilder().setMessage(message).build();
    try {
      EchoResponse response = blockingStub.echo(request);
      LOG.info(response.getMessage());
    } catch (StatusRuntimeException e) {
      LOG.error("Call to EchoService::echo failed", e);
    }
  }
}
