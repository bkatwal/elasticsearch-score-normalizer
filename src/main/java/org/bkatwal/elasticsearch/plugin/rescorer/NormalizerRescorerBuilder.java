package org.bkatwal.elasticsearch.plugin.rescorer;

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryRewriteContext;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.rescore.RescoreContext;
import org.elasticsearch.search.rescore.RescorerBuilder;

import java.io.IOException;

import static org.bkatwal.elasticsearch.plugin.rescorer.NormalizerType.isValid;

public class NormalizerRescorerBuilder extends RescorerBuilder<NormalizerRescorerBuilder> {
  public static final String NAME = "score_normalizer";

  private static final ParseField NORMALIZER_TYPE = new ParseField("normalizer_type");
  private static final ParseField MIN_SCORE = new ParseField("min_score");
  private static final ParseField MAX_SCORE = new ParseField("max_score");
  private static final ParseField FACTOR = new ParseField("factor");
  private static final ParseField FACTOR_MODE = new ParseField("factor_mode");
  private static final ParseField ON_SCORES_SAME = new ParseField("on_score_same");
  private static final float DEFAULT_MIN_SCORE_V = 1.0f;
  private static final float DEFAULT_MAX_SCORE_V = 5.0f;
  private static final float DEFAULT_FACTOR = 0.0f;
  private static final String DEFAULT_ON_SCORES_SAME = MinMaxSameScoreStrategy.avg.name();

  private static final NormalizerType DEFAULT_NORMALIZER_TYPE = NormalizerType.z_score;
  private static final String DEFAULT_FACTOR_MODE =
      NormalizerFactorMathOp.increase_by_percent.name();

  private float minScore;
  private float maxScore;
  private String normalizerType;
  private float factor;
  private String factorMode;
  private String onScoresSame;

  private static final ObjectParser<NRCoreBuilder, Void> NORMALIZER_RESCORER_PARSER =
      new ObjectParser<>(NAME, null);

  static {
    NORMALIZER_RESCORER_PARSER.declareString(
        NormalizerRescorerBuilder.NRCoreBuilder::setNormalizerType, NORMALIZER_TYPE);
    NORMALIZER_RESCORER_PARSER.declareFloat(
        NormalizerRescorerBuilder.NRCoreBuilder::setMinScore, MIN_SCORE);
    NORMALIZER_RESCORER_PARSER.declareFloat(
        NormalizerRescorerBuilder.NRCoreBuilder::setMaxScore, MAX_SCORE);
    NORMALIZER_RESCORER_PARSER.declareFloat(
        NormalizerRescorerBuilder.NRCoreBuilder::setFactor, FACTOR);
    NORMALIZER_RESCORER_PARSER.declareString(
        NormalizerRescorerBuilder.NRCoreBuilder::setFactorMode, FACTOR_MODE);
    NORMALIZER_RESCORER_PARSER.declareString(
        NormalizerRescorerBuilder.NRCoreBuilder::setOnScoresSame, ON_SCORES_SAME);
  }

  public NormalizerRescorerBuilder() {}

  public NormalizerRescorerBuilder(StreamInput in) throws IOException {
    super(in);
    normalizerType = in.readOptionalString();
    minScore = in.readOptionalFloat();
    maxScore = in.readOptionalFloat();
    factor = in.readOptionalFloat();
    factorMode = in.readOptionalString();
    onScoresSame = in.readOptionalString();
  }

  @Override
  protected void doWriteTo(StreamOutput out) throws IOException {
    out.writeString(normalizerType);
    out.writeFloat(minScore);
    out.writeFloat(maxScore);
    out.writeFloat(factor);
    out.writeString(factorMode);
    out.writeString(onScoresSame);
  }

  @Override
  protected void doXContent(XContentBuilder builder, Params params) throws IOException {
    builder.startObject(NAME);
    builder.field(NORMALIZER_TYPE.getPreferredName(), normalizerType);
    builder.field(MIN_SCORE.getPreferredName(), minScore);
    builder.field(MAX_SCORE.getPreferredName(), maxScore);
    builder.field(FACTOR.getPreferredName(), factor);
    builder.field(FACTOR_MODE.getPreferredName(), factorMode);
    builder.field(ON_SCORES_SAME.getPreferredName(), onScoresSame);
    builder.endObject();
  }

  @Override
  protected RescoreContext innerBuildContext(int windowSize, QueryShardContext context)
      throws IOException {
    ScoreNormalizerRescorer.ScoreNormalizerRescorerContext scoreNormalizerRescorerContext =
        new ScoreNormalizerRescorer.ScoreNormalizerRescorerContext(
            windowSize, normalizerType, minScore, maxScore, factor, factorMode, onScoresSame);
    // query is rewritten at this point already
    scoreNormalizerRescorerContext.setNormalizerType(normalizerType);
    scoreNormalizerRescorerContext.setMinScore(minScore);
    scoreNormalizerRescorerContext.setMaxScore(maxScore);
    scoreNormalizerRescorerContext.setFactor(factor);
    scoreNormalizerRescorerContext.setFactorMode(factorMode);
    scoreNormalizerRescorerContext.setOnScoresSame(onScoresSame);
    return scoreNormalizerRescorerContext;
  }

  public static NormalizerRescorerBuilder fromXContent(XContentParser parser) throws IOException {
    NormalizerRescorerBuilder.NRCoreBuilder nrCoreBuilder =
        NORMALIZER_RESCORER_PARSER.parse(
            parser, new NormalizerRescorerBuilder.NRCoreBuilder(), null);
    return nrCoreBuilder.build();
  }

  @Override
  public String getWriteableName() {
    return NAME;
  }

  @Override
  public RescorerBuilder<NormalizerRescorerBuilder> rewrite(QueryRewriteContext ctx)
      throws IOException {
    return this;
  }

  public NormalizerRescorerBuilder setMinScore(float minScore) {
    this.minScore = minScore;
    return this;
  }

  public NormalizerRescorerBuilder setFactor(float factor) {
    this.factor = factor;
    return this;
  }

  public NormalizerRescorerBuilder setFactorMode(String factorMode) {
    this.factorMode = factorMode;
    return this;
  }

  public NormalizerRescorerBuilder setMaxScore(float maxScore) {
    this.maxScore = maxScore;
    return this;
  }

  public NormalizerRescorerBuilder setNormalizerType(String normalizerType) {
    this.normalizerType = normalizerType;
    return this;
  }

  public NormalizerRescorerBuilder setOnScoresSame(String onScoresSame) {
    this.onScoresSame = onScoresSame;
    return this;
  }

  private static class NRCoreBuilder {

    private float minScore = DEFAULT_MIN_SCORE_V;
    private float maxScore = DEFAULT_MAX_SCORE_V;
    private String normalizerType = DEFAULT_NORMALIZER_TYPE.name();
    private float factor = DEFAULT_FACTOR;
    private String factorMode = DEFAULT_FACTOR_MODE;
    private String onScoresSame = DEFAULT_ON_SCORES_SAME;

    NormalizerRescorerBuilder build() {
      NormalizerRescorerBuilder normalizerRescorerBuilder = new NormalizerRescorerBuilder();
      normalizerRescorerBuilder.setNormalizerType(normalizerType);
      normalizerRescorerBuilder.setMinScore(minScore);
      normalizerRescorerBuilder.setMaxScore(maxScore);
      normalizerRescorerBuilder.setFactor(factor);
      normalizerRescorerBuilder.setFactorMode(factorMode);
      normalizerRescorerBuilder.setOnScoresSame(onScoresSame);
      return normalizerRescorerBuilder;
    }

    public void setMinScore(float minScore) {
      this.minScore = minScore;
    }

    public void setMaxScore(float maxScore) {
      this.maxScore = maxScore;
    }

    public void setFactor(float factor) {
      this.factor = factor;
    }

    public void setFactorMode(String factorMode) {
      this.factorMode = factorMode;
    }

    public void setOnScoresSame(String onScoresSame) {
      this.onScoresSame = onScoresSame;
    }

    public void setNormalizerType(String normalizerType) {
      if (normalizerType == null) {
        return;
      }
      if (isValid(normalizerType)) {
        this.normalizerType = normalizerType;
      }
    }
  }
}
