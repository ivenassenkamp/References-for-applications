package me.mypvp.base.network.exceptions;

public class ResponseException extends BaseNetworkException {

  private static final long serialVersionUID = 6435162491000063238L;

  public ResponseException(ResponseExceptionType exception) {
    super(exception.getDescription());
  }

  public enum ResponseExceptionType {

    UNKNOWN_ERROR("Unknown error"),
    NO_HANDLERS("There was no handler to process this package"),
    NOT_HANDLED("No handler has responded to the packet"),
    RESPONSE_HANDLE_ERROR("An error occurred while trying to process the response");

    private final String description;

    ResponseExceptionType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

  }

}
