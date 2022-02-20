package it.tccr.jseq;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;

import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static org.apache.commons.lang3.ArrayUtils.subarray;

@Slf4j
public class JsonSeqParser {

  static final byte recordSeparator = 0x1e;

  public List<String> parse(byte[] bytes) {
    return findJsons(bytes)
      .map(Json::getContent)
      .collect(List.collector());
  }

  private List<Json> findJsons(byte[] bytes) {
    List<Validation<String, Json>> jsons = List.empty();
    var lastRecordSeparatorIndex = Option.<Integer>none();
    for (int index = 0; index < bytes.length; index++) {
      if (isRecordSeparator(bytes[index]) && lastRecordSeparatorIndex.isEmpty()) {
        var json = toJson(bytes, 0, index);
        jsons = jsons.append(json);
        lastRecordSeparatorIndex = Option.some(index);
      } else if (isRecordSeparator(bytes[index]) && lastRecordSeparatorIndex.isDefined()) {
        var json = toJson(bytes, lastRecordSeparatorIndex.get(), index);
        jsons = jsons.append(json);
        lastRecordSeparatorIndex = Option.some(index);
      } else if (index == bytes.length - 1) {
        var json = toJson(bytes, lastRecordSeparatorIndex.getOrElse(0), bytes.length);
        jsons = jsons.append(json);
      }
    }
    return jsons.flatMap(Validation::toOption);
  }

  private boolean isRecordSeparator(byte recordSeparatorByte) {
    return recordSeparator == recordSeparatorByte;
  }

  private Validation<String, Json> toJson(byte[] bytes, int startIndexInclusive, int endIndexExclusive) {
    return toRecord(bytes, startIndexInclusive, endIndexExclusive)
      .flatMap(this::validateJson);
  }

  private Validation<String, byte[]> toRecord(byte[] bytes, int startIndexInclusive, int endIndexExclusive) {
    byte[] record = subarray(bytes, startIndexInclusive, endIndexExclusive);
    log.trace("Validate record {}", new String(record));
    return validateNotEmpty(record).flatMap(this::validateStartOfRecord);
  }

  private Validation<String, byte[]> validateNotEmpty(byte[] bytes) {
    return bytes.length > 0 ? valid(bytes) : invalid("Record is empty");
  }

  private Validation<String, byte[]> validateStartOfRecord(byte[] bytes) {
    return isRecordSeparator(bytes[0])
      ? valid(subarray(bytes, 1, bytes.length))
      : invalid("Missing start character RS");
  }

  private Validation<String, Json> validateJson(byte[] record) {
    return Validation.<String, byte[]>valid(record)
      .map(String::new)
      .map(Json::of); // TODO Check if JSON is valid
  }
}
