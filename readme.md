# Elasticsearch Score Normalizer
Plugin to normalize score using Min Max or Z Score normalizer and updates normalized score by a 
given `factor` and `factor_mode`.
This plugin is built on top of elasticsearch's rescorer feature and normalizes the top docs.

If page `size` is 10 and `from` offset is 20. Then all 30 docs will be normalized.

### Common Attributes
#### normalizer_type (Optional)
Type of the normalizer. Accepts `z_score` or `min_max`, if nothing passed defaults to z_score.

#### factor (Optional)
A float value. If passed updates the normalized score using this factor and given operation
(`factor_mode`)
#### factor_mode (Optional)
Tells how to combine normalized score and the `factor`. Accepted values are `sum`, `multiply` 
and `increase_by_percent`. `increase_by_percent` increases the score by given factor(values 
from 0 to 1)


### Min Max Normalizer

#### Attributes
`min_score` - minimum score value of the returned top docs. Default is 1.

`max_score` - maximum score value of the returned top docs. Default is 5.

`on_score_same` - Normalize strategy when all docs score are same. Accepted values are `avg`
(average of max and min), `max`(maximum value), `min`(minimum value)

Normalize scores between given `max_score` and `min_score`. If no `min_score` and `max_score` is 
passed, defaults to 1 and 5 respectively.

Example:
```json
       
{
  "query": {
    ... some query
  },
  "from" : 0,
  "size" : 50,
  "rescore" : {
      "score_normalizer" : {
        "normalizer_type" : "min_max",
        "min_score" : 1,
        "max_score" : 10
      }
   }
}
```

### Z Score Normalizer
Normalize scores using Z Score. 

Below example first normalizes the scores using z-score and then increase the score by 60 percent.
Example:
```json
{
  "query": {
    ... some query
  },
  "from" : 0,
  "size" : 50,
  "rescore" : {
      "score_normalizer" : {
        "normalizer_type" : "z_score",
        "min_score" : 1,
        "factor" : 0.6,
        "factor_mode" : "increase_by_percent"
      }
   }
}
```




