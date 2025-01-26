package eu.assessment.swisspost.prices.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateAssetPricesOnStartup implements ApplicationListener<ApplicationReadyEvent> {

  private final UpdateAssetPricesTimer updateAssetPricesTimer;

  @Value("${swiss.asset-price-updater.trigger-initial-load}")
  private boolean triggerInitialLoad;

  public UpdateAssetPricesOnStartup(UpdateAssetPricesTimer updateAssetPricesTimer) {
    this.updateAssetPricesTimer = updateAssetPricesTimer;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {

    if (this.triggerInitialLoad) {
      updateAssetPricesTimer.initialLoadOfAssets();
    }
  }
}
