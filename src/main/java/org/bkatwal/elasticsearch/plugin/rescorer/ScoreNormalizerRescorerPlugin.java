package org.bkatwal.elasticsearch.plugin.rescorer;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;

import java.util.List;

import static java.util.Collections.singletonList;

public class ScoreNormalizerRescorerPlugin extends Plugin implements SearchPlugin {
  @Override
  public List<SearchPlugin.RescorerSpec<?>> getRescorers() {
    return singletonList(
        new SearchPlugin.RescorerSpec<>(
            NormalizerRescorerBuilder.NAME,
            NormalizerRescorerBuilder::new,
            NormalizerRescorerBuilder::fromXContent));
  }
}
