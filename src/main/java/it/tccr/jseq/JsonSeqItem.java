package it.tccr.jseq;

import lombok.Getter;
import lombok.Value;

@Value(staticConstructor = "of")
@Getter
public class JsonSeqItem {

  String content;
}
