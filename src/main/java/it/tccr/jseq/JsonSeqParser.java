package it.tccr.jseq;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.commons.lang3.ArrayUtils;

public class JsonSeqParser {

  static final byte recordSeparator = 0x1e;

  public List<String> parse(byte[] bytes) {
    var records = findRecordsFrom(bytes);

    return records
      .flatMap(this::toJsonSeqItem)
      .map(JsonSeqItem::getContent)
      .collect(List.collector());
  }

  private List<byte[]> findRecordsFrom(byte[] bytes) {
    var recordIndexes = List.<Tuple2<Integer, Integer>>empty();
    var lastRecordSeparatorIndex = Option.<Integer>none();
    for (int index = 0; index < bytes.length; index++) {
      if (isRecordSeparator(bytes[index]) && lastRecordSeparatorIndex.isEmpty()) {
        recordIndexes = recordIndexes.append(Tuple.of(0, index));
        lastRecordSeparatorIndex = Option.some(index);
      } else if (isRecordSeparator(bytes[index]) && lastRecordSeparatorIndex.isDefined()) {
        recordIndexes = recordIndexes.append(Tuple.of(lastRecordSeparatorIndex.get(), index));
        lastRecordSeparatorIndex = Option.some(index);
      } else if (index == bytes.length - 1) {
        recordIndexes = recordIndexes.append(Tuple.of(lastRecordSeparatorIndex.getOrElse(0), bytes.length));
      }
    }
    return recordIndexes
      .map(recordBoundaries -> ArrayUtils.subarray(bytes, recordBoundaries._1, recordBoundaries._2));
  }

  private boolean isRecordSeparator(byte recordSeparatorByte) {
    return recordSeparator == recordSeparatorByte;
  }

  private Try<JsonSeqItem> toJsonSeqItem(byte[] bytes) {
    return Try.of(() -> bytes)
      .map(String::new)
      .filter(content -> !content.isBlank())
      .filter(content -> content.startsWith(new String(new byte[] { recordSeparator })))
      .map(content -> content.substring(1))
      .map(JsonSeqItem::of);
  }
}
