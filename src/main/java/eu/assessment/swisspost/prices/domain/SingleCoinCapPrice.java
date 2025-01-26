package eu.assessment.swisspost.prices.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleCoinCapPrice {
  private AssetInfo data;
  private long timestamp;
}
