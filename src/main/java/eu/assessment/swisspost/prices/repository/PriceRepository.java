package eu.assessment.swisspost.prices.repository;

import eu.assessment.swisspost.prices.domain.entity.Price;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<Price, UUID> {
  Optional<Price> findBySymbol(String symbol);
}
