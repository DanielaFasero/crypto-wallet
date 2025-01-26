package eu.assessment.swisspost.utils.exceptions;

public class UserNotFoundException extends RuntimeException {
  private static final String USER_NOT_FOUND_ERROR =
      "User was not found with the following email: %s";

  public UserNotFoundException(final String email) {
    super(String.format(USER_NOT_FOUND_ERROR, email));
  }
}
