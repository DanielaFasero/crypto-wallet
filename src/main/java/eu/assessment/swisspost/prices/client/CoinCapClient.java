package eu.assessment.swisspost.prices.client;

import eu.assessment.swisspost.prices.domain.AssetCoinCapHistory;
import eu.assessment.swisspost.prices.domain.MultipleCoinCapPrice;
import eu.assessment.swisspost.prices.domain.SingleCoinCapPrice;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
@Getter
public class CoinCapClient {

  private final WebClient.Builder webClientBuilder;
  private WebClient webClient;

  @Value("${swiss.coin-cap-url:https://api.coincap.io/v2}")
  private String coinCapUrl;

  public CoinCapClient(WebClient.Builder webClientBuilder) {
    this.webClientBuilder = webClientBuilder;
  }

  // Note: as when spring starts the coinCapUrl might be there right away,
  // we need to update the webclient at a later date (aka PostContruct)
  @PostConstruct
  private void initWebClient() {
    this.webClient = webClientBuilder.baseUrl(this.coinCapUrl).build();
  }

  public Flux<SingleCoinCapPrice> getAssetPrices(Mono<List<String>> assetSymbols) {
    return assetSymbols.flatMapMany(
        symbols ->
            Flux.fromIterable(symbols)
                .buffer(3)
                .flatMap(batch -> Flux.fromIterable(batch).flatMap(this::fetchAssetData)));
  }

  public Flux<SingleCoinCapPrice> getSingleAssetPrices(String symbolId) {
    return fetchAssetData(symbolId);
  }

  public Flux<MultipleCoinCapPrice> getAllAssetPrices() {
    return fetchAllAssetsInfo();
  }

  public Flux<AssetCoinCapHistory> getHistoryForAsset(
      List<String> listOfAssets, Long unixStartDate, Long unixEndDate) {

    return Flux.fromIterable(listOfAssets)
        .flatMapSequential(symbol -> this.fetchAssetHistory(symbol, unixStartDate, unixEndDate));
  }

  private Flux<AssetCoinCapHistory> fetchAssetHistory(
      String symbol, Long unixStartDate, Long unixEndDate) {
    return this.webClient
        .get()
        .uri(
            "/assets/{symbol}/history?interval=d1&start={unixStartDate}&end={unixEndDate}",
            symbol,
            unixStartDate,
            unixEndDate)
        .retrieve()
        .bodyToFlux(AssetCoinCapHistory.class);
  }

  private Flux<MultipleCoinCapPrice> fetchAllAssetsInfo() {
    return this.webClient
        .get()
        .uri("/assets")
        .header(HttpHeaders.CONTENT_ENCODING, "gzip")
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .retrieve()
        .bodyToFlux(MultipleCoinCapPrice.class);
  }

  private Flux<SingleCoinCapPrice> fetchAssetData(String assetSymbol) {
    return this.webClient
        .get()
        .uri("/assets/{symbol}", assetSymbol)
        .header(HttpHeaders.CONTENT_ENCODING, "gzip")
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .retrieve()
        .bodyToFlux(SingleCoinCapPrice.class);
  }
}
