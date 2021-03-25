package uk.me.maitland.grpc.todo;

import io.grpc.Context;
import io.grpc.Metadata;

public class Constants {
  public static final Metadata.Key<String> JWT =
      Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER);
  public static final Context.Key<String> DECODED_JWT = Context.key("JWT");
}
