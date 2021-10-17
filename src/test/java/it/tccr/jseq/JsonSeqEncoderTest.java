package it.tccr.jseq;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSeqEncoderTest {

  @Test
  void encodeJsonSeqShouldSucceed() throws FileNotFoundException {
    var encoder = new JsonSeqEncoder();
    encoder.encode(Stream.empty(), new FileOutputStream("output.txt"));
    assertThat(true).isTrue(); // FIXME
  }
}
