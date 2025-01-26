package eu.assessment.swisspost.prices.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultipleCoinCapPrice {
  private List<AssetInfo> data;
  private long timestamp;
}
