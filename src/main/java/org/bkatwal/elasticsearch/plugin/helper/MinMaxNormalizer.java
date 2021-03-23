/*
The MIT License (MIT)

Copyright (c) Bikas Katwal - bikas.katwal10@gmail.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package org.bkatwal.elasticsearch.plugin.helper;

import org.apache.lucene.search.TopDocs;
import org.bkatwal.elasticsearch.plugin.rescorer.MinMaxSameScoreStrategy;
import org.bkatwal.elasticsearch.plugin.rescorer.NormalizerFactorMathOp;
import org.bkatwal.elasticsearch.plugin.rescorer.ScoreNormalizerRescorer;

public class MinMaxNormalizer implements Normalizer {

  @Override
  public TopDocs normalize(
      TopDocs topDocs, ScoreNormalizerRescorer.ScoreNormalizerRescorerContext context) {

    if (context.getMinScore() >= context.getMaxScore()) {
      throw new IllegalArgumentException(
          "max_score can not be lesser than or equal to  " + "min_score");
    }
    if (topDocs.scoreDocs.length == 0) {
      return topDocs;
    }
    if (topDocs.scoreDocs.length == 1) {
      topDocs.scoreDocs[0].score =
          getFinalScore(context.getFactorMode(), context.getFactor(), context.getMaxScore());
      return topDocs;
    }

    float oldMax = topDocs.scoreDocs[0].score;
    float oldMin = topDocs.scoreDocs[topDocs.scoreDocs.length - 1].score;

    if (Float.compare(oldMax, oldMin) == 0) {
      for (int i = 0; i < topDocs.scoreDocs.length; i++) {
        if (context.getOnScoresSame().equals(MinMaxSameScoreStrategy.avg.name())) {
          topDocs.scoreDocs[i].score = (context.getMaxScore() + context.getMinScore()) / 2;
        } else if (context.getOnScoresSame().equals(MinMaxSameScoreStrategy.min.name())) {
          topDocs.scoreDocs[i].score = context.getMinScore();
        } else if (context.getOnScoresSame().equals(MinMaxSameScoreStrategy.max.name())) {
          topDocs.scoreDocs[i].score = context.getMaxScore();
        } else {
          topDocs.scoreDocs[i].score = (context.getMaxScore() + context.getMinScore()) / 2;
        }
      }
      return topDocs;
    }

    for (int i = 0; i < topDocs.scoreDocs.length; i++) {
      float normalizedScore =
          calculate(
              topDocs.scoreDocs[i].score,
              oldMin,
              oldMax,
              context.getMaxScore(),
              context.getMinScore());

      topDocs.scoreDocs[i].score =
          getFinalScore(context.getFactorMode(), context.getFactor(), normalizedScore);
    }
    if (topDocs.scoreDocs.length > 2) {
      topDocs.scoreDocs[0].score =
          topDocs.scoreDocs[0].score + (topDocs.scoreDocs[0].score - topDocs.scoreDocs[1].score);
    }
    return topDocs;
  }

  private static float calculate(float v, float oldMin, float oldMax, float newMax, float newMin) {
    return ((v - oldMin) / (oldMax - oldMin)) * (newMax - newMin) + newMin;
  }

  private static float getFinalScore(String factorMode, float factor, float normalizedValue) {

    if (factorMode.equals(NormalizerFactorMathOp.sum.name())) {
      normalizedValue = normalizedValue + factor;
    } else if (factorMode.equals(NormalizerFactorMathOp.multiply.name())) {
      normalizedValue = normalizedValue * factor;
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
        normalizedValue = normalizedValue + normalizedValue * factor;
      }
    }
    return normalizedValue;
  }
}
