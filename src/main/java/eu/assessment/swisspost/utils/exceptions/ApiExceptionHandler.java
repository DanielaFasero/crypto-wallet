package eu.assessment.swisspost.utils.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<String> handleUserAlreadyExists(UserAlreadyExistsException ex) {
    return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(String.format("{\"message\":\"%s\"}", ex.getMessage()));
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(String.format("{\"message\":\"%s\"}", ex.getMessage()));
  }

  @ExceptionHandler(AssetIsntKnownException.class)
  public ResponseEntity<String> handleAssetNotFoundException(AssetIsntKnownException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(String.format("{\"message\":\"%s\"}", ex.getMessage()));
  }
}
