package it.tccr.jseq;

import lombok.Getter;
import lombok.Value;

@Value(staticConstructor = "of")
@Getter
public class Json {

  public enum Type {
    ARRAY,
    BOOLEAN,
    NULL,
    NUMBER,
    OBJECT,
    STRING
  }

  Type type;
  String content;
}
