package eu.assessment.swisspost.user.domain;

import java.util.Set;
import java.util.UUID;

public record WalletDTO(UUID id, Double total, Set<AssetDTO> assets) {}
