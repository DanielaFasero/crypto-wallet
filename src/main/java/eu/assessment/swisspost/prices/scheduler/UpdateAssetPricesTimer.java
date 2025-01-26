package eu.assessment.swisspost.prices.scheduler;

import eu.assessment.swisspost.prices.client.CoinCapClient;
import eu.assessment.swisspost.prices.domain.MultipleCoinCapPrice;
import eu.assessment.swisspost.prices.domain.SingleCoinCapPrice;
import eu.assessment.swisspost.prices.service.PriceUpdaterService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@AllArgsConstructor
public class UpdateAssetPricesTimer {

  private final CoinCapClient coinCapClient;
  private final PriceUpdaterService priceUpdaterService;

  @Scheduled(cron = "${asset-price-updater.timer.cron}")
  public void updateAssetPricesTimer() {
    log.info("Updating asset prices");
    Mono<List<String>> listOfAllAssets = priceUpdaterService.fetchAllExistingAssets();
    Flux<SingleCoinCapPrice> assetPrices = coinCapClient.getAssetPrices(listOfAllAssets);

    // todo: change from CoinCapPrice to something internal perhaps??
    priceUpdaterService.updateAssetsInfo(assetPrices);

    log.info("Done updating asset prices");
  }

  public void initialLoadOfAssets() {
    Flux<MultipleCoinCapPrice> allAssetPrices = coinCapClient.getAllAssetPrices();

    priceUpdaterService.updateMultipleAssetsInfo(allAssetPrices);
  }
}
