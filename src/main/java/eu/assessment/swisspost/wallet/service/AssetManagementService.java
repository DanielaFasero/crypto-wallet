package eu.assessment.swisspost.wallet.service;

import eu.assessment.swisspost.prices.client.CoinCapClient;
import eu.assessment.swisspost.prices.domain.AssetCoinCapHistory;
import eu.assessment.swisspost.prices.domain.entity.Price;
import eu.assessment.swisspost.prices.repository.PriceRepository;
import eu.assessment.swisspost.user.repository.UserRepository;
import eu.assessment.swisspost.utils.Triple;
import eu.assessment.swisspost.utils.exceptions.AssetIsntKnownException;
import eu.assessment.swisspost.utils.exceptions.UserNotFoundException;
import eu.assessment.swisspost.wallet.domain.AddToWalletRequest;
import eu.assessment.swisspost.wallet.domain.AddToWalletResponse;
import eu.assessment.swisspost.wallet.domain.AssetForWalletEvaluation;
import eu.assessment.swisspost.wallet.domain.WalletEvaluationResponse;
import eu.assessment.swisspost.wallet.domain.entity.Asset;
import eu.assessment.swisspost.wallet.domain.entity.Wallet;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@AllArgsConstructor
@Slf4j
public class AssetManagementService {

  private final CoinCapClient coinCapClient;
  private final UserRepository userRepository;
  private final PriceRepository priceRepository;

  public Mono<AddToWalletResponse> addToWallet(AddToWalletRequest addToWalletRequest) {
    return Mono.fromCallable(
            () -> this.userRepository.findByEmail(addToWalletRequest.getUserEmail()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            user -> {
              if (user.isEmpty()) {
                return Mono.error(new UserNotFoundException(addToWalletRequest.getUserEmail()));
              }

              // todo: this is can lead to thread starvation, please fix it
              String currencyFullName =
                  this.priceRepository
                      .findBySymbol(addToWalletRequest.getSymbol().toUpperCase())
                      .get()
                      .getSymbolId();

              return this.coinCapClient
                  .getSingleAssetPrices(currencyFullName)
                  .next()
                  .flatMap(
                      assetPrice -> {
                        Wallet wallet = user.get().getWallet();
                        boolean assetExists =
                            wallet.getAssets().stream()
                                .anyMatch(
                                    asset ->
                                        asset.getSymbol().equals(addToWalletRequest.getSymbol()));

                        if (assetExists) {
                          wallet
                              .getAssets()
                              .forEach(
                                  asset -> {
                                    if (asset.getSymbol().equals(addToWalletRequest.getSymbol())) {
                                      Double assetCurrentValue =
                                          calculateCurrentValue(addToWalletRequest, wallet, true);
                                      asset.setQuantity(
                                          asset.getQuantity() + addToWalletRequest.getQuantity());
                                      asset.setPrice(addToWalletRequest.getPrice());
                                      asset.setCurrentValue(assetCurrentValue);
                                      wallet.setTotal(
                                          calculateTotal(wallet.getTotal(), assetCurrentValue));
                                    }
                                  });
                        } else {
                          Double currentValueOfAsset =
                              calculateCurrentValue(addToWalletRequest, wallet, false);

                          Asset newAsset = new Asset();
                          newAsset.setSymbol(addToWalletRequest.getSymbol());
                          newAsset.setQuantity(addToWalletRequest.getQuantity());
                          newAsset.setPrice(addToWalletRequest.getPrice());
                          newAsset.setCurrentValue(currentValueOfAsset);
                          newAsset.setWallet(wallet);
                          wallet.setTotal(calculateTotal(wallet.getTotal(), currentValueOfAsset));
                          wallet.getAssets().add(newAsset);
                        }

                        return Mono.fromCallable(() -> userRepository.save(user.get()))
                            .map(
                                savedUser ->
                                    new AddToWalletResponse("Asset successfully added to wallet"))
                            .subscribeOn(Schedulers.boundedElastic());
                      });
            })
        .onErrorResume(
            e -> Mono.error(new UserNotFoundException(addToWalletRequest.getUserEmail())));
  }

  public Mono<WalletEvaluationResponse> retrieveAssetsHistory(
      List<AssetForWalletEvaluation> assetForWalletEvaluations,
      LocalDate startDateTime,
      LocalDate endDateTime) {

    // Map -> key = assetName, Pair(value paid for 1 unit, highest value in time window, quantity
    // bought)
    Map<String, Triple<Double, Double, Double>> assetsEvaluationMap = new HashMap<>();

    Long unixStartTime = startDateTime.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    Long unixEndTime = endDateTime.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();

    List<String> assets =
        assetForWalletEvaluations.stream()
            .map(
                assetInWallet -> {
                  Optional<Price> assetInDb =
                      this.priceRepository.findBySymbol(assetInWallet.symbol());

                  if (assetInDb.isPresent()) {
                    String symbol = assetInDb.get().getSymbolId();
                    Double unitValue = findUnitValueForPaidAsset(assetInWallet);
                    assetsEvaluationMap.put(
                        assetInWallet.symbol(),
                        new Triple<>(unitValue, 0.0, assetInWallet.quantity()));
                    return symbol;
                  } else throw new AssetIsntKnownException(assetInWallet.symbol());
                })
            .toList();

    // todo: make me pretty
    return coinCapClient
        .getHistoryForAsset(assets, unixStartTime, unixEndTime)
        .collectList()
        .flatMap(
            assetCoinCapHistories ->
                compareAssetValuesAndGetWalletEvaluation(
                    assetsEvaluationMap, assetCoinCapHistories))
        .doOnError(error -> log.error("Error occurred while getting assets history: ", error));
  }

  private Double findUnitValueForPaidAsset(AssetForWalletEvaluation assetInWallet) {

    BigDecimal pricePairForAsset = BigDecimal.valueOf(assetInWallet.price());
    BigDecimal quantityBought = BigDecimal.valueOf(assetInWallet.quantity());
    var unitValue = pricePairForAsset.divide(quantityBought, 3, RoundingMode.HALF_DOWN);

    return unitValue.doubleValue();
  }

  private Mono<WalletEvaluationResponse> compareAssetValuesAndGetWalletEvaluation(
      Map<String, Triple<Double, Double, Double>> assetsEvaluationMap,
      List<AssetCoinCapHistory> listWithHistoryOfAssets) {

    List<Map.Entry<String, Triple<Double, Double, Double>>> entryList =
        assetsEvaluationMap.entrySet().stream().toList();
    for (int i = 0; i < entryList.size(); i++) {
      Double max = listWithHistoryOfAssets.get(i).data().getFirst().priceUsd();

      for (int j = 0; j < listWithHistoryOfAssets.getFirst().data().size(); j++) {
        if (max < listWithHistoryOfAssets.get(i).data().get(j).priceUsd()) {
          max = listWithHistoryOfAssets.get(i).data().get(j).priceUsd();
        }
      }

      Double valuePaidForUnit = assetsEvaluationMap.get(entryList.get(i).getKey()).first();
      Double quantityBought = assetsEvaluationMap.get(entryList.get(i).getKey()).third();
      assetsEvaluationMap.replace(
          entryList.get(i).getKey(), new Triple<>(valuePaidForUnit, max, quantityBought));
    }

    // Map -> key = assetName, Triple(value paid for 1 unit, highest value in time window, quantity
    // bought)
    double total =
        assetsEvaluationMap.values().stream().map(it -> it.second() * it.third()).toList().stream()
            .mapToDouble(Double::doubleValue)
            .sum();

    Map.Entry<String, Triple<Double, Double, Double>> highestEntry = null;
    Map.Entry<String, Triple<Double, Double, Double>> lowestEntry = null;

    for (Map.Entry<String, Triple<Double, Double, Double>> entry : assetsEvaluationMap.entrySet()) {
      if (highestEntry == null || entry.getValue().second() > highestEntry.getValue().second()) {
        highestEntry = entry;
      }
      if (lowestEntry == null || entry.getValue().second() < lowestEntry.getValue().second()) {
        lowestEntry = entry;
      }
    }

    Pair<String, Double> bestAsset = Pair.of(highestEntry.getKey(), getPercentage(highestEntry));
    Pair<String, Double> notTheBest = Pair.of(lowestEntry.getKey(), getPercentage(lowestEntry));

    return Mono.just(
        new WalletEvaluationResponse(
            total,
            bestAsset.getFirst(),
            bestAsset.getSecond(),
            notTheBest.getFirst(),
            notTheBest.getSecond()));
  }

  private Double getPercentage(Map.Entry<String, Triple<Double, Double, Double>> entry) {
    // Map -> key = assetName, Triple(value paid for 1 unit, highest value in time window, quantity
    // bought)

    BigDecimal up = BigDecimal.valueOf(entry.getValue().second());
    BigDecimal down = BigDecimal.valueOf(entry.getValue().first());
    var division = up.divide(down, 2, RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(100));

    return division.doubleValue();
  }

  private Double calculateCurrentValue(
      AddToWalletRequest addToWalletRequest, Wallet wallet, boolean assetExists) {
    if (assetExists) {
      return wallet.getAssets().stream()
          .filter(asset -> asset.getSymbol().equals(addToWalletRequest.getSymbol()))
          .map(
              asset ->
                  asset.getCurrentValue()
                      + (addToWalletRequest.getPrice() * addToWalletRequest.getQuantity()))
          .toList()
          .getFirst();
    }
    return addToWalletRequest.getPrice() * addToWalletRequest.getQuantity();
  }

  private Double calculateTotal(Double total, Double currentValueOfAsset) {
    return (total != null) ? total + currentValueOfAsset : currentValueOfAsset;
  }

  private Double transformIntoDouble(String priceUsd) {
    // todo: improve this as this is ugly :)
    String formattedPrice = String.format("%.3f", Double.parseDouble(priceUsd));
    return Double.parseDouble(formattedPrice);
  }
}
