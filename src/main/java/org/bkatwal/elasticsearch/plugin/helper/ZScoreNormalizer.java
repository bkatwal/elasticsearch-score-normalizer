package org.bkatwal.elasticsearch.plugin.helper;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.bkatwal.elasticsearch.plugin.rescorer.NormalizerFactorMathOp;
import org.bkatwal.elasticsearch.plugin.rescorer.ScoreNormalizerRescorer;

public class ZScoreNormalizer implements Normalizer {
  @Override
  public TopDocs normalize(
      TopDocs topDocs, ScoreNormalizerRescorer.ScoreNormalizerRescorerContext rescoreContext) {

    if (topDocs.scoreDocs.length == 0) {
      return topDocs;
    }

    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
    float mean = meanScore(scoreDocs);
    float sd = standardDeviation(scoreDocs, mean);

    if (sd == 0.0f) {
      sd = 1.0f;
    }
    for (ScoreDoc scoreDoc : scoreDocs) {
      float normalizedScore = zScore(scoreDoc.score, mean, sd);

      scoreDoc.score =
          getFinalScore(
              rescoreContext.getFactorMode(), rescoreContext.getFactor(), normalizedScore);
    }
    return topDocs;
  }

  private float meanScore(ScoreDoc[] scoreDocs) {

    float total = 0.0f;

    for (ScoreDoc scoreDoc : scoreDocs) {
      total = total + scoreDoc.score;
    }

    return total / scoreDocs.length;
  }

  private float standardDeviation(ScoreDoc[] scoreDocs, float mean) {

    float totalVariance = 0.0f;
    for (ScoreDoc scoreDoc : scoreDocs) {
      float delta = scoreDoc.score - mean;
      float deltaSq = delta * delta;
      totalVariance = totalVariance + deltaSq;
    }
    return (float) Math.sqrt(totalVariance / scoreDocs.length);
  }

  private float zScore(float val, float mean, float sd) {

    return (val - mean) / sd;
  }

  private static float getFinalScore(String factorMode, float factor, float normalizedValue) {

    if (factorMode.equals(NormalizerFactorMathOp.sum.name())) {
      normalizedValue = normalizedValue + factor;
    } else {
      float v = normalizedValue + Math.abs(normalizedValue) * factor;
      if (factorMode.equals(NormalizerFactorMathOp.multiply.name())) {
        normalizedValue = normalizedValue >= 0 ? normalizedValue * factor : v;
      } else {
        if (normalizedValue == 0.0f) {
          normalizedValue = factor;
        } else {
          if (factor < 0 || factor > 1) {
            throw new IllegalArgumentException(
                "Invalid `factor` for `factor_mode` "
                    + "increase_by_percent, "
                    + "allowed "
                    + "factor "
                    + "range "
                    + "0-1 "
                    + "including 0 and 1.");
          }
          normalizedValue = v;
        }
      }
    }
    return normalizedValue;
  }
}
