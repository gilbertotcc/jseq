package it.tccr.jseq;

import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.util.stream.Stream;

@AllArgsConstructor
public class JsonSeqDecoder {

  public Stream<Object> decode(InputStream input) {
    return Stream.empty(); // FIXME
  }
}
