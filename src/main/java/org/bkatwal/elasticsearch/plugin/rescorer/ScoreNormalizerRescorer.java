package org.bkatwal.elasticsearch.plugin.rescorer;

import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.bkatwal.elasticsearch.plugin.helper.NormalizerServiceLocator;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.search.rescore.RescoreContext;
import org.elasticsearch.search.rescore.Rescorer;

import static java.util.Collections.singletonList;

public class ScoreNormalizerRescorer implements Rescorer {

  public static final Rescorer INSTANCE = new ScoreNormalizerRescorer();

  /**
   * this function returns the top k normalized docs from each shard.
   *
   * @param topDocs top docs matched for given query
   * @param searcher Index Searcher
   * @param rescoreContext Context/params needed for rescore function.
   * @return return top k normalized docs from each shard
   */
  @Override
  public TopDocs rescore(TopDocs topDocs, IndexSearcher searcher, RescoreContext rescoreContext) {

    assert rescoreContext != null;
    if (topDocs == null || topDocs.scoreDocs.length == 0) {
      return topDocs;
    }

    ScoreNormalizerRescorerContext context = (ScoreNormalizerRescorerContext) rescoreContext;
    String normalizerType = context.normalizerType;

    topDocs =
        NormalizerServiceLocator.getInstance(NormalizerType.valueOf(normalizerType))
            .normalize(topDocs, context);
    return topDocs;
  }

  @Override
  public Explanation explain(
      int topLevelDocId,
      IndexSearcher searcher,
      RescoreContext rescoreContext,
      Explanation sourceExplanation) {

    ScoreNormalizerRescorerContext context = (ScoreNormalizerRescorerContext) rescoreContext;
    String factorMode = context.getFactorMode();
    float factor = context.factor;
    String operation = factorMode + " using " + factor + " on:";

    return Explanation.match(
        0.0f,
        "Final score -> normalize using, " + context.getNormalizerType() + " and then " + operation,
        singletonList(sourceExplanation));
  }

  public static class ScoreNormalizerRescorerContext extends RescoreContext {
    private String normalizerType;
    private float minScore;
    private float maxScore;
    private float factor;
    private String factorMode;
    private String onScoresSame;

    public ScoreNormalizerRescorerContext(
        int windowSize,
        @Nullable String normalizerType,
        @Nullable float minScore,
        @Nullable float maxScore,
        @Nullable float factor,
        @Nullable String factorMode,
        @Nullable String onScoresSame) {
      super(windowSize, INSTANCE);
      this.minScore = minScore;
      this.maxScore = maxScore;
      this.normalizerType = normalizerType;
      this.factorMode = factorMode;
      this.factor = factor;
      this.onScoresSame = onScoresSame;
    }

    public void setFactor(float factor) {
      this.factor = factor;
    }

    public void setFactorMode(String factorMode) {
      this.factorMode = factorMode;
    }

    public String getNormalizerType() {
      return normalizerType;
    }

    public void setNormalizerType(String normalizerType) {
      this.normalizerType = normalizerType;
    }

    public float getMinScore() {
      return minScore;
    }

    public float getFactor() {
      return factor;
    }

    public String getOnScoresSame() {
      return onScoresSame;
    }

    public void setOnScoresSame(String onScoresSame) {
      this.onScoresSame = onScoresSame;
    }

    public String getFactorMode() {
      return factorMode;
    }

    public void setMinScore(float minScore) {
      this.minScore = minScore;
    }

    public float getMaxScore() {
      return maxScore;
    }

    public void setMaxScore(float maxScore) {
      this.maxScore = maxScore;
    }
  }
}
