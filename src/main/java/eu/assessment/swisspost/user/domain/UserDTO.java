package eu.assessment.swisspost.user.domain;

import java.util.UUID;

public record UserDTO(UUID id, String email, WalletDTO wallet) {}
