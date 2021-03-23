package org.bkatwal.elasticsearch.plugin.rescorer;

public enum NormalizerType {
    min_max, z_score;

    public static boolean isValid(String normalizerType) {
        try {
            NormalizerType.valueOf(normalizerType);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
