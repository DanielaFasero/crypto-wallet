package eu.assessment.swisspost.utils.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

  private static final String USER_ALREADY_EXISTS_ERROR =
      "User with the following email already exists: %s";

  public UserAlreadyExistsException(final String email) {
    super(String.format(USER_ALREADY_EXISTS_ERROR, email));
  }
}
