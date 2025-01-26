package eu.assessment.swisspost.wallet.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public record WalletEvaluationRequest(
    List<AssetForWalletEvaluation> assets,
    @JsonFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
    @JsonFormat(pattern = "dd/MM/yyyy") LocalDate endDate) {}
