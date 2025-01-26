package eu.assessment.swisspost.wallet.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToWalletRequest {
  private String userEmail;
  private String symbol;
  private Double price;
  private Double quantity;
}
