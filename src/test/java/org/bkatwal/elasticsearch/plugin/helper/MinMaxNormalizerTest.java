package org.bkatwal.elasticsearch.plugin.helper;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;
import org.bkatwal.elasticsearch.plugin.rescorer.MinMaxSameScoreStrategy;
import org.bkatwal.elasticsearch.plugin.rescorer.ScoreNormalizerRescorer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MinMaxNormalizerTest {

  private Normalizer minMaxNormalizer;

  @Before
  public void init() {
    minMaxNormalizer = new MinMaxNormalizer();
  }

  @Test
  public void assertMinMaxNormalizer() {
    TotalHits totalHits = new TotalHits(5, TotalHits.Relation.EQUAL_TO);
    ScoreDoc[] scoreDocs = new ScoreDoc[5];
    scoreDocs[0] = new ScoreDoc(1, 10.5f);
    scoreDocs[1] = new ScoreDoc(2, 9);
    scoreDocs[2] = new ScoreDoc(3, 8);
    scoreDocs[3] = new ScoreDoc(4, 6.5f);
    scoreDocs[4] = new ScoreDoc(5, 2f);
    TopDocs topDocs = new TopDocs(totalHits, scoreDocs);

    ScoreNormalizerRescorer.ScoreNormalizerRescorerContext context =
        new ScoreNormalizerRescorer.ScoreNormalizerRescorerContext(
            3, "min_max", 1, 4, .6f, "increase_by_percent", null);

    topDocs = minMaxNormalizer.normalize(topDocs, context);
    Assert.assertEquals(7.2f, topDocs.scoreDocs[0].score, 0.1f);
    Assert.assertEquals(5.5, topDocs.scoreDocs[1].score, 0.1f);
    Assert.assertEquals(4.9, topDocs.scoreDocs[2].score, 0.1f);
    Assert.assertEquals(4.1f, topDocs.scoreDocs[3].score, 0.1f);
    Assert.assertEquals(1.6f, topDocs.scoreDocs[4].score, 0.1f);
  }

  @Test
  public void assertMinMaxNormalizerZeroDocs() {
    TotalHits totalHits = new TotalHits(0, TotalHits.Relation.EQUAL_TO);
    ScoreDoc[] scoreDocs = new ScoreDoc[0];
    TopDocs topDocs = new TopDocs(totalHits, scoreDocs);

    ScoreNormalizerRescorer.ScoreNormalizerRescorerContext context =
        new ScoreNormalizerRescorer.ScoreNormalizerRescorerContext(
            3, "min_max", 1, 4, .6f, "increase_by_percent", null);

    topDocs = minMaxNormalizer.normalize(topDocs, context);
    Assert.assertNotNull(topDocs);
  }

  @Test
  public void assertMinMaxNormalizerOneDocs() {
    TotalHits totalHits = new TotalHits(1, TotalHits.Relation.EQUAL_TO);
    ScoreDoc[] scoreDocs = new ScoreDoc[1];
    TopDocs topDocs = new TopDocs(totalHits, scoreDocs);
    scoreDocs[0] = new ScoreDoc(1, 10.5f);

    ScoreNormalizerRescorer.ScoreNormalizerRescorerContext context =
        new ScoreNormalizerRescorer.ScoreNormalizerRescorerContext(
            3, "min_max", 1, 4, .6f, "increase_by_percent", null);

    topDocs = minMaxNormalizer.normalize(topDocs, context);
    Assert.assertEquals(6.4f, topDocs.scoreDocs[0].score, 0.0f);
  }

  @Test
  public void assertSameScoreDocs() {
    TotalHits totalHits = new TotalHits(5, TotalHits.Relation.EQUAL_TO);
    ScoreDoc[] scoreDocs = new ScoreDoc[5];
    scoreDocs[0] = new ScoreDoc(1, 4f);
    scoreDocs[1] = new ScoreDoc(2, 4f);
    scoreDocs[2] = new ScoreDoc(3, 4f);
    scoreDocs[3] = new ScoreDoc(4, 4f);
    scoreDocs[4] = new ScoreDoc(5, 4f);
    TopDocs topDocs = new TopDocs(totalHits, scoreDocs);

    ScoreNormalizerRescorer.ScoreNormalizerRescorerContext context =
        new ScoreNormalizerRescorer.ScoreNormalizerRescorerContext(
            3, "min_max", 1, 5, .6f, "increase_by_percent", MinMaxSameScoreStrategy.avg.name());

    topDocs = minMaxNormalizer.normalize(topDocs, context);
    Assert.assertEquals(3f, topDocs.scoreDocs[0].score, 0.0f);
    Assert.assertEquals(3f, topDocs.scoreDocs[1].score, 0.0f);
    Assert.assertEquals(3f, topDocs.scoreDocs[2].score, 0.0f);
    Assert.assertEquals(3f, topDocs.scoreDocs[3].score, 0.0f);
    Assert.assertEquals(3f, topDocs.scoreDocs[4].score, 0.0f);
  }

  @Test
  public void assertValueSameAsNewMin() {
    TotalHits totalHits = new TotalHits(2, TotalHits.Relation.EQUAL_TO);
    ScoreDoc[] scoreDocs = new ScoreDoc[2];
    TopDocs topDocs = new TopDocs(totalHits, scoreDocs);
    scoreDocs[0] = new ScoreDoc(1, 10);
    scoreDocs[1] = new ScoreDoc(2, 5);

    ScoreNormalizerRescorer.ScoreNormalizerRescorerContext context =
        new ScoreNormalizerRescorer.ScoreNormalizerRescorerContext(
            2, "min_max", 5, 20, 0.0f, "increase_by_percent", null);

    topDocs = minMaxNormalizer.normalize(topDocs, context);
    Assert.assertEquals(20f, topDocs.scoreDocs[0].score, 0.0f);
    Assert.assertEquals(5f, topDocs.scoreDocs[1].score, 0.0f);
  }
}
