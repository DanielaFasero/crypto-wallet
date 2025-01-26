package eu.assessment.swisspost.wallet.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Asset {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private String symbol;
  private Double quantity;
  private Double price;
  private Double currentValue;

  @ManyToOne
  @JoinColumn(name = "wallet_id")
  private Wallet wallet;
}
