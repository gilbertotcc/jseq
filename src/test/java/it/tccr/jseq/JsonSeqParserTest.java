package it.tccr.jseq;

import io.vavr.collection.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class JsonSeqParserTest {

  @ParameterizedTest
  @MethodSource("validJsonSequences")
  void parseEmptyJsonSequenceShouldSucceed(String jsonSequence, List<String> expectedJson) {
    // given
    var parser = new JsonSeqParser();
    var input = jsonSequence.getBytes(StandardCharsets.UTF_8);

    // when
    var fields = parser.parse(input);

    // then
    assertThat(fields).containsExactlyElementsOf(expectedJson);
  }

  private static Stream<Arguments> validJsonSequences() {
    return Stream.of(
      arguments("", List.empty()),
      arguments("\u001e", List.empty()),
      arguments("\u001e\"xyz\"", List.of("\"xyz\"")),
      arguments("\u001e1.23", List.of("1.23")),
      arguments("\u001e{ \"field\": \"value\" }", List.of("{ \"field\": \"value\" }")),
      arguments("\u001e[]", List.of("[]")),
      arguments("\u001etrue", List.of("true")),
      arguments("\u001enull", List.of("null"))
    );
  }
}
