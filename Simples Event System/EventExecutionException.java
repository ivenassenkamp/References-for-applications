package me.mypvp.base.event;

public class EventExecutionException extends RuntimeException {

  private static final long serialVersionUID = -8762968776825344826L;

  public EventExecutionException(String message) {
    super(message);
  }

  public EventExecutionException(Throwable cause) {
    super(cause);
  }

  public EventExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public EventExecutionException() {
  }

}
