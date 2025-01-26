package eu.assessment.swisspost.prices.service;

import eu.assessment.swisspost.prices.domain.AssetInfo;
import eu.assessment.swisspost.prices.domain.MultipleCoinCapPrice;
import eu.assessment.swisspost.prices.domain.SingleCoinCapPrice;
import eu.assessment.swisspost.prices.domain.entity.Price;
import eu.assessment.swisspost.prices.repository.PriceRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriceUpdaterService {

  private final PriceRepository priceRepository;

  public void updateAssetsInfo(Flux<SingleCoinCapPrice> assetPrices) {
    assetPrices
        .doOnNext(it -> log.info("Started updating prices"))
        .flatMap(
            assetInfo ->
                // Create a Mono from Optional returned by findBySymbol
                Mono.justOrEmpty(this.priceRepository.findBySymbol(assetInfo.getData().getSymbol()))
                    .flatMap(
                        existingPrice -> {
                          // If the price exists, update it
                          return Mono.fromCallable(
                                  () ->
                                      this.priceRepository.save(
                                          convertFromCoinToPrice(
                                              existingPrice, assetInfo.getData())))
                              .subscribeOn(Schedulers.boundedElastic());
                        })
                    .switchIfEmpty(
                        // If no price found, create a new one and save it
                        Mono.fromCallable(
                                () ->
                                    this.priceRepository.save(
                                        convertFromCoinToPrice(new Price(), assetInfo.getData())))
                            .subscribeOn(Schedulers.boundedElastic())))
        .doOnNext(it -> log.info("Successfully updated the asset price"))
        .doOnError(error -> log.error("Error updating asset prices", error))
        .subscribe(); // Trigger the pipeline
  }

  public void updateMultipleAssetsInfo(Flux<MultipleCoinCapPrice> assetPrices) {
    assetPrices
        .doOnNext(it -> log.info("Started initial load of asset prices"))
        .flatMap(
            multipleCoinCapPrice ->
                Flux.fromIterable(multipleCoinCapPrice.getData())
                    .flatMap(
                        assetInfo ->
                            Mono.justOrEmpty(
                                    this.priceRepository.findBySymbol(assetInfo.getSymbol()))
                                .flatMap(
                                    existingPrice -> {
                                      // If the price exists, update it
                                      return Mono.fromCallable(
                                              () ->
                                                  this.priceRepository.save(
                                                      convertFromCoinToPrice(
                                                          existingPrice, assetInfo)))
                                          .subscribeOn(Schedulers.boundedElastic());
                                    })
                                .switchIfEmpty(
                                    // If no price found, create a new one and save it
                                    Mono.fromCallable(
                                            () ->
                                                this.priceRepository.save(
                                                    convertFromCoinToPrice(new Price(), assetInfo)))
                                        .subscribeOn(Schedulers.boundedElastic()))))
        .doOnNext(it -> log.info("Finished successfully initial load of asset prices"))
        .doOnError(error -> log.error("Error while doing initial load", error))
        .subscribe();
  }

  public Mono<List<String>> fetchAllExistingAssets() {

    return Mono.just(this.priceRepository.findAll().stream().map(Price::getSymbolId).toList());
  }

  private Price convertFromCoinToPrice(Price existingPrice, AssetInfo assetInfo) {
    existingPrice.setCurrentValue(transformIntoDouble(assetInfo.getPriceUsd()));
    existingPrice.setSymbolId(assetInfo.getId());
    existingPrice.setLastUpdate(OffsetDateTime.now());
    existingPrice.setSymbol(assetInfo.getSymbol());
    return existingPrice;
  }

  private Double transformIntoDouble(String priceUsd) {
    // todo: improve this as this is ugly :)
    String formattedPrice = String.format("%.3f", Double.parseDouble(priceUsd));
    return Double.parseDouble(formattedPrice);
  }
}
