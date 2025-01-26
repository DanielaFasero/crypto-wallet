package eu.assessment.swisspost.wallet.domain;

public record WalletEvaluationResponse(
    Double total,
    String bestAsset,
    Double bestPerformance,
    String worstAsset,
    Double worstPerformance) {}
