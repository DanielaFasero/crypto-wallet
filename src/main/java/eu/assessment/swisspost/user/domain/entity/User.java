package eu.assessment.swisspost.user.domain.entity;

import eu.assessment.swisspost.wallet.domain.entity.Wallet;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "crypto_user")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(unique = true)
  private String email;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private Wallet wallet;

  public User(String email) {
    this.email = email;
    this.wallet = new Wallet();
    this.wallet.setUser(this);
  }
}
