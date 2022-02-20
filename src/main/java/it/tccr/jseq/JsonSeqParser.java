package it.tccr.jseq;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.commons.lang3.ArrayUtils;

public class JsonSeqParser {

  static final byte recordSeparator = 0x1e;

  public List<String> parse(byte[] bytes) {
    var records = findRecordsFrom(bytes);

    return records
      .flatMap(this::toJson)
      .map(Json::getContent)
      .collect(List.collector());
  }

  private List<byte[]> findRecordsFrom(byte[] bytes) {
    var records = List.<byte[]>empty();
    var lastRecordSeparatorIndex = Option.<Integer>none();
    for (int index = 0; index < bytes.length; index++) {
      if (isRecordSeparator(bytes[index]) && lastRecordSeparatorIndex.isEmpty()) {
        records = records.append(ArrayUtils.subarray(bytes, 0, index - 1));
        lastRecordSeparatorIndex = Option.some(index);
      } else if (isRecordSeparator(bytes[index]) && lastRecordSeparatorIndex.isDefined()) {
        records = records.append(ArrayUtils.subarray(bytes, lastRecordSeparatorIndex.get(), index - 1));
        lastRecordSeparatorIndex = Option.some(index);
      } else if (index == bytes.length - 1) {
        records = records.append(ArrayUtils.subarray(bytes, lastRecordSeparatorIndex.getOrElse(0), bytes.length));
      }
    }
    return records;
  }

  private boolean isRecordSeparator(byte recordSeparatorByte) {
    return recordSeparator == recordSeparatorByte;
  }

  private Try<Json> toJson(byte[] bytes) {
    return Try.of(() -> bytes)
      .map(String::new)
      .filter(content -> !content.isBlank())
      .filter(content -> content.startsWith(new String(new byte[] { recordSeparator })))
      .map(content -> content.substring(1))
      .map(Json::of);
  }
}
