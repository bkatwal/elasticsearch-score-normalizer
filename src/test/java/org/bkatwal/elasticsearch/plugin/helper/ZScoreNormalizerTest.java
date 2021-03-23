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

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;
import org.bkatwal.elasticsearch.plugin.rescorer.ScoreNormalizerRescorer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ZScoreNormalizerTest {
  private Normalizer zScoreNormalizer;

  @Before
  public void init() {
    zScoreNormalizer = new ZScoreNormalizer();
  }

  @Test
  public void assertNormalization() {
    TotalHits totalHits = new TotalHits(5, TotalHits.Relation.EQUAL_TO);
    ScoreDoc[] scoreDocs = new ScoreDoc[5];
    scoreDocs[0] = new ScoreDoc(1, 10f);
    scoreDocs[1] = new ScoreDoc(2, 6f);
    scoreDocs[2] = new ScoreDoc(3, 4f);
    scoreDocs[3] = new ScoreDoc(4, 3.5f);
    scoreDocs[4] = new ScoreDoc(5, 3f);
    TopDocs topDocs = new TopDocs(totalHits, scoreDocs);

    ScoreNormalizerRescorer.ScoreNormalizerRescorerContext context =
        new ScoreNormalizerRescorer.ScoreNormalizerRescorerContext(
            5, "z_score", 1, 4, 0.0f, "increase_by_percent", null);

    topDocs = zScoreNormalizer.normalize(topDocs, context);
    Assert.assertEquals(1.8f, topDocs.scoreDocs[0].score, 0.1f);
    Assert.assertEquals(0.27f, topDocs.scoreDocs[1].score, 0.1f);
    Assert.assertEquals(-0.5f, topDocs.scoreDocs[2].score, 0.1f);
    Assert.assertEquals(-0.7f, topDocs.scoreDocs[3].score, 0.1f);
    Assert.assertEquals(-0.89, topDocs.scoreDocs[4].score, 0.1f);
  }

  @Test
  public void assertNormalizeSameScore() {
    TotalHits totalHits = new TotalHits(5, TotalHits.Relation.EQUAL_TO);
    ScoreDoc[] scoreDocs = new ScoreDoc[3];
    scoreDocs[0] = new ScoreDoc(1, 10f);
    scoreDocs[1] = new ScoreDoc(2, 10f);
    scoreDocs[2] = new ScoreDoc(3, 10f);
    TopDocs topDocs = new TopDocs(totalHits, scoreDocs);

    ScoreNormalizerRescorer.ScoreNormalizerRescorerContext context =
        new ScoreNormalizerRescorer.ScoreNormalizerRescorerContext(
            3, "z_score", 1, 4, 0.0f, "increase_by_percent", null);

    topDocs = zScoreNormalizer.normalize(topDocs, context);
    Assert.assertEquals(0.0f, topDocs.scoreDocs[0].score, 0.1f);
    Assert.assertEquals(0.0f, topDocs.scoreDocs[1].score, 0.1f);
    Assert.assertEquals(0.0f, topDocs.scoreDocs[2].score, 0.1f);
  }

  @Test
  public void assertFactorWithNormalization() {
    TotalHits totalHits = new TotalHits(3, TotalHits.Relation.EQUAL_TO);
    ScoreDoc[] scoreDocs = new ScoreDoc[5];
    scoreDocs[0] = new ScoreDoc(1, 10f);
    scoreDocs[1] = new ScoreDoc(2, 6f);
    scoreDocs[2] = new ScoreDoc(3, 4f);
    scoreDocs[3] = new ScoreDoc(4, 3.5f);
    scoreDocs[4] = new ScoreDoc(5, 3f);
    TopDocs topDocs = new TopDocs(totalHits, scoreDocs);

    ScoreNormalizerRescorer.ScoreNormalizerRescorerContext context =
        new ScoreNormalizerRescorer.ScoreNormalizerRescorerContext(
            5, "z_score", 1, 4, 0.5f, "increase_by_percent", null);

    topDocs = zScoreNormalizer.normalize(topDocs, context);
    Assert.assertEquals(2.7f, topDocs.scoreDocs[0].score, 0.1f);
    Assert.assertEquals(0.405f, topDocs.scoreDocs[1].score, 0.1f);
    Assert.assertEquals(-0.25f, topDocs.scoreDocs[2].score, 0.1f);
  }
}
