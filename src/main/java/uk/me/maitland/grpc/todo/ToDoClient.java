package uk.me.maitland.grpc.todo;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import uk.me.maitland.grpc.auth.AuthGrpc;
import uk.me.maitland.grpc.auth.AuthOuterClass.AuthenticationRequest;
import uk.me.maitland.grpc.auth.AuthOuterClass.AuthenticationResponse;
import uk.me.maitland.grpc.todo.Todo.GetTasksRequest;
import uk.me.maitland.grpc.todo.Todo.Task;

@Slf4j
public class ToDoClient {
  public static void main(String[] args) throws InterruptedException {
    AuthGrpcClient authGrpcClient =
        new AuthGrpcClient(ManagedChannelBuilder.forAddress("maitland.me.uk", 5001).build());

    String jwt = authGrpcClient.authenticate("iain", "password");
    Metadata metadata = new Metadata();
    metadata.put(Constants.JWT, jwt);

    ToDoGrpcClient toDoGrpcClient =
        new ToDoGrpcClient(
            ManagedChannelBuilder.forAddress("maitland.me.uk", 5001)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build());

    toDoGrpcClient.addTask("Make tea");
  }

  @Slf4j
  private static class ToDoGrpcClient {
    private final ToDoGrpc.ToDoBlockingStub blockingStub;
    private final ToDoGrpc.ToDoStub asyncStub;

    public ToDoGrpcClient(Channel channel) {
      blockingStub = ToDoGrpc.newBlockingStub(channel);
      asyncStub = ToDoGrpc.newStub(channel);
    }

    public void addTask(String description) {
      try {
        Task task = blockingStub.addTask(Task.newBuilder().setDescription(description).build());
        log.info(getPrettyString(task));
      } catch (StatusRuntimeException e) {
        log.error("Error calling ToDo/addTask", e);
      }
    }

    public CountDownLatch addTasks(List<Task> tasks) throws InterruptedException {
      CountDownLatch countDownLatch = new CountDownLatch(1);
      StreamObserver<Task> streamObserver =
          asyncStub.addTasks(
              new StreamObserver<>() {
                @Override
                public void onNext(Task value) {
                  log.info(getPrettyString(value));
                }

                @Override
                public void onError(Throwable t) {
                  log.error("Error calling ToDo/addTasks", t);
                  countDownLatch.countDown();
                }

                @Override
                public void onCompleted() {
                  log.info("Finished!");
                  countDownLatch.countDown();
                }
              });

      try {
        for (Task task : tasks) {
          log.info("Adding task: {}", task.getDescription());
          streamObserver.onNext(task);
        }
      } catch (RuntimeException e) {
        streamObserver.onError(e);
        throw e;
      }

      streamObserver.onCompleted();
      return countDownLatch;
    }

    public void getTask(String id) {
      try {
        Task task = blockingStub.getTask(Task.newBuilder().setId(id).build());
        log.info(getPrettyString(task));
      } catch (StatusRuntimeException e) {
        log.error("Error calling ToDo/addTask", e);
      }
    }

    public void getTasks() {
      try {
        Iterator<Task> tasks = blockingStub.getTasks(GetTasksRequest.getDefaultInstance());
        while (tasks.hasNext()) {
          Task task = tasks.next();
          log.info(getPrettyString(task));
        }
      } catch (StatusRuntimeException e) {
        log.error("Error calling ToDo/getTasks", e);
      }
    }

    private String getPrettyString(Task task) {
      return String.format("ID: %s, Description: %s", task.getId(), task.getDescription());
    }
  }

  @Slf4j
  public static class AuthGrpcClient {
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
}
