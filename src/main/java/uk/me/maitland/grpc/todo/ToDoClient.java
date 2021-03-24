package uk.me.maitland.grpc.todo;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import uk.me.maitland.grpc.todo.Todo.GetTasksRequest;
import uk.me.maitland.grpc.todo.Todo.Task;

@Slf4j
public class ToDoClient {
  public static void main(String[] args) throws InterruptedException {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 5001).usePlaintext().build();

    ToDoGrpcClient toDoGrpcClient = new ToDoGrpcClient(channel);

    toDoGrpcClient.addTask("Make tea");
    toDoGrpcClient.getTask("75b8c3f0-1f53-41df-a112-ed314631cacb");
    CountDownLatch countDownLatch =
        toDoGrpcClient.addTasks(
            List.of(
                Task.newBuilder().setDescription("Finish the gRPC tutorial").build(),
                Task.newBuilder().setDescription("Make tea").build(),
                Task.newBuilder().setDescription("Give Kara a hug").build()));

    if (!countDownLatch.await(1, TimeUnit.MINUTES)) {
      log.warn("ToDo/addTasks cannot finish within 1 minutes");
    }

    toDoGrpcClient.getTasks();
  }

  @Slf4j
  private static class ToDoGrpcClient {
    private ToDoGrpc.ToDoBlockingStub blockingStub;
    private ToDoGrpc.ToDoStub asyncStub;

    public ToDoGrpcClient(Channel channel) {
      this.blockingStub = ToDoGrpc.newBlockingStub(channel);
      this.asyncStub = ToDoGrpc.newStub(channel);
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
}
