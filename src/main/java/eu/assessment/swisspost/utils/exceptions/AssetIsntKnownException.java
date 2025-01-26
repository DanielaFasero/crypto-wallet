package eu.assessment.swisspost.utils.exceptions;

public class AssetIsntKnownException extends RuntimeException {
  private static final String ASSET_NOT_FOUND_ERROR =
      "Asset was not found with the following symbol: %s";

  public AssetIsntKnownException(final String symbol) {
    super(String.format(ASSET_NOT_FOUND_ERROR, symbol));
  }
}
