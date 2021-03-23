package org.bkatwal.elasticsearch.plugin.helper;

import org.bkatwal.elasticsearch.plugin.rescorer.NormalizerType;

public final class NormalizerServiceLocator {

  private NormalizerServiceLocator() {}

  private static final Normalizer minMaxNormalizer = new MinMaxNormalizer();
  private static final Normalizer zScoreNormalizer = new ZScoreNormalizer();

  public static Normalizer getInstance(NormalizerType normalizerType) {
    if (normalizerType == NormalizerType.min_max) {
      return minMaxNormalizer;
    }
    if (normalizerType == NormalizerType.z_score) {
      return zScoreNormalizer;
    }

    return zScoreNormalizer;
  }
}
