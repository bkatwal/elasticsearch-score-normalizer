package org.bkatwal.elasticsearch.plugin.helper;

import org.apache.lucene.search.TopDocs;
import org.bkatwal.elasticsearch.plugin.rescorer.ScoreNormalizerRescorer;

public interface Normalizer {

  TopDocs normalize(
      TopDocs topDocs, ScoreNormalizerRescorer.ScoreNormalizerRescorerContext rescoreContext);
}
