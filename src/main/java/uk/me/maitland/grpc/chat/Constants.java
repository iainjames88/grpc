package uk.me.maitland.grpc.chat;

import io.grpc.Context;
import io.grpc.Metadata;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

public class Constants {
  public static final Metadata.Key<String> JWT =
      Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER);
  public static final Context.Key<Jws<Claims>> DECODED_JWT = Context.key("JWT");
  public static final Key SIGNING_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
}
