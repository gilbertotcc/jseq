package it.tccr.jseq;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSeqDecoderTest {

  @Test
  void decodeJsonSeqShouldSucceed() throws FileNotFoundException {
    var decoder = new JsonSeqDecoder();
    var jsonFields = decoder.decode(new FileInputStream(""));
    assertThat(jsonFields.count()).isEqualTo(1); // FIXME
  }
}
