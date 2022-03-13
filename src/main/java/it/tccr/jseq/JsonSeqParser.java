package it.tccr.jseq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static org.apache.commons.lang3.ArrayUtils.subarray;

@Slf4j
public class JsonSeqParser {

  static final byte recordSeparator = 0x1e;

  static final ObjectMapper objectMapper = new ObjectMapper();

  public List<Json> parse(byte[] bytes) {
    return findJsons(bytes)
      .collect(List.collector());
  }

  private List<Json> findJsons(byte[] bytes) {
    List<Validation<String, Json>> jsons = List.empty();
    int jsonStartIndex = 0;
    int currentIndex = 0;
    while (currentIndex < bytes.length) {
      if (isRecordSeparator(bytes[currentIndex])) {
        var json = toJson(bytes, jsonStartIndex, currentIndex);
        jsons = jsons.append(json);
        jsonStartIndex = currentIndex;
      }
      currentIndex++;
    }
    if (jsonStartIndex < currentIndex) {
      var json = toJson(bytes, jsonStartIndex, bytes.length);
      jsons = jsons.append(json);
    }
    return jsons.flatMap(Validation::toOption);
  }

  private boolean isRecordSeparator(byte recordSeparatorByte) {
    return recordSeparator == recordSeparatorByte;
  }

  private Validation<String, Json> toJson(byte[] bytes, int startIndexInclusive, int endIndexExclusive) {
    byte[] recordBytes = subarray(bytes, startIndexInclusive, endIndexExclusive);
    return toRecord(recordBytes)
      .flatMap(r -> validateJson(r).mapError(errors -> "Cannot validate JSON")) // FIXME
      .mapError(validationError -> "Invalid JSON at indexes %d-%d. Error: %s"
        .formatted(startIndexInclusive, endIndexExclusive - 1, validationError)
      );
  }

  private Validation<String, byte[]> toRecord(byte[] recordBytes) {
    return validateRecordFormat(recordBytes);
  }

  private Validation<String, byte[]> validateRecordFormat(byte[] recordBytes) {
    return recordBytes.length > 1 && isRecordSeparator(recordBytes[0])
      ? valid(subarray(recordBytes, 1, recordBytes.length))
      : invalid("Invalid record format");
  }

  private Validation<Seq<String>, Json> validateJson(byte[] recordBytes) {
    return Validation.<String, String>valid(new String(recordBytes))
      .combine(validateJsonFormat(recordBytes))
      .ap((content, type) -> Json.of(type, content));
  }

  private Validation<String, Json.Type> validateJsonFormat(byte[] content) {
    return Try.of(() -> objectMapper.readTree(content))
      .toValidation(error -> "Error occurred while parsing JSON content. Error: %s".formatted(error.getMessage()))
      .map(JsonNode::getNodeType)
      .flatMap(type -> Match(type).of(
        Case($(JsonNodeType.ARRAY), valid(Json.Type.ARRAY)),
        Case($(JsonNodeType.BOOLEAN), valid(Json.Type.BOOLEAN)),
        Case($(JsonNodeType.NULL), valid(Json.Type.NULL)),
        Case($(JsonNodeType.NUMBER), valid(Json.Type.NUMBER)),
        Case($(JsonNodeType.OBJECT), valid(Json.Type.OBJECT)),
        Case($(JsonNodeType.STRING), valid(Json.Type.STRING)),
        Case($(), invalid("Unknown JSON type"))
      ));
  }
}
