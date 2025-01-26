package eu.assessment.swisspost.user.domain;

import org.springframework.http.HttpStatus;

public record UserInformationResponse(
    String outputMessage, WalletDTO wallet, HttpStatus httpStatus) {}
