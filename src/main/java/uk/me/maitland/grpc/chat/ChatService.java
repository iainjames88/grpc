package uk.me.maitland.grpc.chat;

import static uk.me.maitland.grpc.chat.ChatGrpc.getStreamChatMethod;

import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import uk.me.maitland.grpc.chat.ChatGrpc.ChatImplBase;
import uk.me.maitland.grpc.chat.ChatOuterClass.Message;

public class ChatService extends ChatImplBase {
  @Override
  public StreamObserver<Message> streamChat(
      StreamObserver<ChatOuterClass.Message> responseObserver) {
    return ServerCalls.asyncUnimplementedStreamingCall(getStreamChatMethod(), responseObserver);
  }
}
