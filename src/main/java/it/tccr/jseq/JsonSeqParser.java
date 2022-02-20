package it.tccr.jseq;

import io.vavr.collection.List;
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
    byte[] record = subarray(bytes, startIndexInclusive, endIndexExclusive);
    return toRecord(record)
      .flatMap(this::validateJson)
      .mapError(validationError -> "Invalid JSON at indexes %d-%d. Error: %s"
        .formatted(startIndexInclusive, endIndexExclusive - 1, validationError)
      );
  }

  private Validation<String, byte[]> toRecord(byte[] record) {
    return validateRecordFormat(record);
  }

  private Validation<String, byte[]> validateRecordFormat(byte[] record) {
    return record.length > 1 && isRecordSeparator(record[0])
      ? valid(subarray(record, 1, record.length))
      : invalid("Invalid record format");
  }

  private Validation<String, Json> validateJson(byte[] record) {
    return Validation.<String, byte[]>valid(record)
      .map(String::new)
      .map(Json::of); // TODO Check if JSON is valid
  }
}
