package eu.assessment.swisspost.wallet.controller;

import eu.assessment.swisspost.wallet.domain.AddToWalletRequest;
import eu.assessment.swisspost.wallet.domain.AddToWalletResponse;
import eu.assessment.swisspost.wallet.domain.WalletEvaluationRequest;
import eu.assessment.swisspost.wallet.domain.WalletEvaluationResponse;
import eu.assessment.swisspost.wallet.service.AssetManagementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/assets")
@Slf4j
@AllArgsConstructor
public class AssetManagementController {

  private final AssetManagementService assetManagementService;

  @PostMapping(value = "/addAsset", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<AddToWalletResponse>> addAsset(
      @RequestBody AddToWalletRequest addToWalletRequest) {
    return assetManagementService
        .addToWallet(addToWalletRequest)
        .map(entity -> ResponseEntity.ok().body(entity));
  }

  @PostMapping(value = "/walletEvaluation", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<WalletEvaluationResponse>> walletEvaluation(
      @RequestBody WalletEvaluationRequest walletEvaluationRequest) {

    return assetManagementService
        .retrieveAssetsHistory(
            walletEvaluationRequest.assets(),
            walletEvaluationRequest.startDate(),
            walletEvaluationRequest.endDate())
        .map(it -> ResponseEntity.ok().body(it));
  }
}
