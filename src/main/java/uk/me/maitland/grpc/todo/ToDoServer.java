package uk.me.maitland.grpc.todo;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import uk.me.maitland.grpc.todo.Todo.GetTasksRequest;
import uk.me.maitland.grpc.todo.Todo.Task;

@Slf4j
public class ToDoServer {
  public static void main(String[] args) {
    int port = 5001;
    InputStream fullChain = ToDoServer.class.getClassLoader().getResourceAsStream("fullchain.pem");
    InputStream privateKey = ToDoServer.class.getClassLoader().getResourceAsStream("privkey.pem");
    Server server =
        ServerBuilder.forPort(port)
            .useTransportSecurity(fullChain, privateKey)
            .addService(new ToDoService())
            .build();

    try {
      log.info("Starting server on port {}", port);
      server.start();
      server.awaitTermination();
    } catch (IOException | InterruptedException e) {
      log.error("Something went wrong", e);
    }
  }

  private static class ToDoService extends ToDoGrpc.ToDoImplBase {
    private static final Map<String, Task> tasks = new HashMap<>();

    @Override
    public void addTask(Task request, StreamObserver<Task> responseObserver) {
      log.info("Handling call to ToDo/addTask");

      String uuid = UUID.randomUUID().toString();
      Task task = Task.newBuilder().setId(uuid).setDescription(request.getDescription()).build();
      tasks.put(uuid, task);

      responseObserver.onNext(task);
      responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Task> addTasks(StreamObserver<Task> responseObserver) {
      return new StreamObserver<>() {
        @Override
        public void onNext(Task value) {
          log.info("Handling call to ToDo/addTasks");

          String id = UUID.randomUUID().toString();
          Task task = Task.newBuilder().setId(id).setDescription(value.getDescription()).build();
          tasks.put(id, task);

          responseObserver.onNext(task);
        }

        @Override
        public void onError(Throwable t) {
          log.error("Something went wrong adding a task", t);
        }

        @Override
        public void onCompleted() {
          responseObserver.onCompleted();
        }
      };
    }

    @Override
    public void getTask(Task request, StreamObserver<Task> responseObserver) {
      log.info("Handling call to ToDo/getTask");

      // TODO how to return an error here?
      Task task = tasks.getOrDefault(request.getId(), Task.getDefaultInstance());

      responseObserver.onNext(task);
      responseObserver.onCompleted();
    }

    @Override
    public void getTasks(GetTasksRequest request, StreamObserver<Task> responseObserver) {
      log.info("Handling call to ToDo/GetTasks");

      tasks.forEach((k, v) -> responseObserver.onNext(v));

      responseObserver.onCompleted();
    }
  }
}
