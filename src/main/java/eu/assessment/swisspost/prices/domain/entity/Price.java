package eu.assessment.swisspost.prices.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Data
public class Price {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(unique = true)
  private String symbol;

  private String symbolId;
  private Double currentValue;
  private OffsetDateTime lastUpdate;

  public Price(String symbol, Double currentValue, OffsetDateTime lastUpdate) {
    this.symbol = symbol;
    this.currentValue = currentValue;
    this.lastUpdate = lastUpdate;
  }
}
